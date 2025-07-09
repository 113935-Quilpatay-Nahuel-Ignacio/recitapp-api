package com.recitapp.recitapp_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

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
}