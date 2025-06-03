package com.recitapp.recitapp_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @PostConstruct
    public void init() {
        System.out.println("🌐 WebConfig initialized - Configuring MVC settings");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        System.out.println("🔗 Configuring CORS for API endpoints");
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("📁 Configuring static resource handlers");
        
        // Explicit resource handlers to avoid conflicts with API endpoints
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        // Ensure API endpoints are NOT treated as static resources
        System.out.println("✅ Static resources configured - API endpoints excluded");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        System.out.println("🛣️ Configuring path matching for REST controllers");
        
        // Ensure REST controllers are properly matched
        configurer.setUseTrailingSlashMatch(true);
        
        System.out.println("✅ Path matching configured for REST endpoints");
    }
}