package com.recitapp.recitapp_api.modules.user.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class GuestUserService {

    /**
     * Verifica si el usuario actual es un invitado (no autenticado)
     */
    public boolean isGuestUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || 
               !authentication.isAuthenticated() || 
               "anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Verifica si una petición viene de un usuario invitado basándose en headers
     */
    public boolean isGuestRequest(HttpServletRequest request) {
        // Verificar si no hay token de autorización
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return true;
        }

        // Verificar si el contexto de seguridad indica usuario anónimo
        return isGuestUser();
    }

    /**
     * Obtiene información del usuario invitado basándose en la petición
     */
    public GuestUserInfo getGuestUserInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = getClientIpAddress(request);
        String sessionId = request.getSession(false) != null ? request.getSession().getId() : null;

        return GuestUserInfo.builder()
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionId(sessionId)
                .isGuest(true)
                .build();
    }

    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Clase para almacenar información del usuario invitado
     */
    public static class GuestUserInfo {
        private String ipAddress;
        private String userAgent;
        private String sessionId;
        private boolean isGuest;

        public static GuestUserInfoBuilder builder() {
            return new GuestUserInfoBuilder();
        }

        public static class GuestUserInfoBuilder {
            private String ipAddress;
            private String userAgent;
            private String sessionId;
            private boolean isGuest;

            public GuestUserInfoBuilder ipAddress(String ipAddress) {
                this.ipAddress = ipAddress;
                return this;
            }

            public GuestUserInfoBuilder userAgent(String userAgent) {
                this.userAgent = userAgent;
                return this;
            }

            public GuestUserInfoBuilder sessionId(String sessionId) {
                this.sessionId = sessionId;
                return this;
            }

            public GuestUserInfoBuilder isGuest(boolean isGuest) {
                this.isGuest = isGuest;
                return this;
            }

            public GuestUserInfo build() {
                GuestUserInfo info = new GuestUserInfo();
                info.ipAddress = this.ipAddress;
                info.userAgent = this.userAgent;
                info.sessionId = this.sessionId;
                info.isGuest = this.isGuest;
                return info;
            }
        }

        // Getters
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        public String getSessionId() { return sessionId; }
        public boolean isGuest() { return isGuest; }
    }
} 