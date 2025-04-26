package com.recitapp.recitapp_api.interceptor;

import com.recitapp.recitapp_api.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }

            if (requireRole != null) {
                String userRole = (String) request.getAttribute("userRole");

                if (userRole == null || !Arrays.asList(requireRole.value()).contains(userRole)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("No tienes permisos para acceder a este recurso");
                    return false;
                }
            }
        }

        return true;
    }
}