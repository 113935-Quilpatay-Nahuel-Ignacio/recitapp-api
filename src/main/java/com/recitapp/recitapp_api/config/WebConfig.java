package com.recitapp.recitapp_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import jakarta.annotation.PostConstruct;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        System.out.println("🌐 WebConfig initialized - Configuring MVC settings");
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
        
        // Handler para archivos subidos - Fixed to use proper path resolution
        System.out.println("🖼️ [CONFIG] Configuring uploads handler for: /uploads/**");
        
        // Use absolute path resolution to match LocalFileStorageServiceImpl
        String uploadsPath = Paths.get(uploadDir).toAbsolutePath().toString();
        uploadsPath = uploadsPath.replace("\\", "/"); // Normalize path separators for URL
        if (!uploadsPath.endsWith("/")) {
            uploadsPath += "/";
        }
        
        System.out.println("🖼️ [CONFIG] Upload directory resolved to: file:" + uploadsPath);
        System.out.println("🖼️ [CONFIG] uploadDir property: " + uploadDir);
        System.out.println("🖼️ [CONFIG] Working directory: " + System.getProperty("user.dir"));
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath)
                .setCachePeriod(3600)
                .resourceChain(true);
        
        // Ensure API endpoints are NOT treated as static resources
        System.out.println("✅ Static resources configured - API endpoints excluded");
        System.out.println("🖼️ [CONFIG] Resource handlers registered:");
        System.out.println("🖼️ [CONFIG] - /uploads/** -> file:" + uploadsPath);
        System.out.println("🖼️ [CONFIG] - /static/** -> classpath:/static/");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        System.out.println("🛣️ Configuring path matching for REST controllers");
        
        // Ensure REST controllers are properly matched
        configurer.setUseTrailingSlashMatch(true);
        
        System.out.println("✅ Path matching configured for REST endpoints");
    }
}