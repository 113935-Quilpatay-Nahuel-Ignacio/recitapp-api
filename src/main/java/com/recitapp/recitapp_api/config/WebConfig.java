package com.recitapp.recitapp_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void configureObjectMapper() {
        // Configurar formateo de fechas para Argentina
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        
        objectMapper.registerModule(javaTimeModule);
        
        // Configurar timezone de Argentina para el ObjectMapper
        objectMapper.setTimeZone(java.util.TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        
        System.out.println("✅ Configuración de timezone Argentina aplicada a ObjectMapper");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Convertir el path a absoluto para evitar problemas
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toString();
        
        
        // Con context path /api, Spring Boot maneja automáticamente /api/uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600) // Cache por 1 hora
                .resourceChain(true);
                
        log.info("🔧 [WEB CONFIG] Configuración de recursos estáticos completada");
        log.info("🔧 [WEB CONFIG] Las URLs serán accesibles como: /api/uploads/categoria/archivo.jpg");
    }
}