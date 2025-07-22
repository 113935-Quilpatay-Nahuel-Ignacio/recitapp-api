package com.recitapp.recitapp_api.config;

import com.recitapp.recitapp_api.modules.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Lista de endpoints públicos que no necesitan autenticación
        boolean isPublicEndpoint = path.startsWith("/api/auth/") ||
               path.startsWith("/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/api/payments/") ||
               path.startsWith("/api/uploads/") ||
               path.startsWith("/uploads/") ||
               path.startsWith("/swagger-ui/") || 
               path.startsWith("/v3/api-docs/") || 
               path.startsWith("/actuator/") ||
               path.startsWith("/h2-console/") ||
               path.equals("/error") ||
               path.equals("/favicon.ico");

        System.out.println("=== JWT FILTER: shouldNotFilter check ===");
        System.out.println("URI: " + path);
        System.out.println("Method: " + method);
        System.out.println("Is Public Endpoint: " + isPublicEndpoint);
        
        if (isPublicEndpoint) {
            System.out.println("✅ JWT FILTER: Skipping public endpoint: " + path);
        } else {
            System.out.println("🔒 JWT FILTER: Will process protected endpoint: " + path);
        }
        
        return isPublicEndpoint;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Double-check: Si es un endpoint de pagos, no procesar NUNCA
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/payments/")) {
            System.out.println("🚨 JWT FILTER: Payment endpoint detected in doFilterInternal - this should NOT happen!");
            System.out.println("URI: " + requestURI);
            System.out.println("Calling filterChain.doFilter without processing...");
            filterChain.doFilter(request, response);
            return;
        }
        
        System.out.println("=== JWT FILTER: Processing protected endpoint ===");
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            System.out.println("Extracted user email: " + userEmail);
        } catch (Exception e) {
            System.out.println("Error extracting username from JWT: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Authentication set successfully for user: " + userEmail);
                } else {
                    System.out.println("JWT token is not valid");
                }
            } catch (Exception e) {
                System.out.println("Error during authentication: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
} 