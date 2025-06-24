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
        // WebConfig initialized
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicit resource handlers to avoid conflicts with API endpoints
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        
        // Handler para archivos subidos
        String uploadsPath = Paths.get(uploadDir).toAbsolutePath().toString();
        uploadsPath = uploadsPath.replace("\\", "/"); // Normalize path separators for URL
        if (!uploadsPath.endsWith("/")) {
            uploadsPath += "/";
        }
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsPath)
                .setCachePeriod(3600)
                .resourceChain(true);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Ensure REST controllers are properly matched
        configurer.setUseTrailingSlashMatch(true);
    }
}