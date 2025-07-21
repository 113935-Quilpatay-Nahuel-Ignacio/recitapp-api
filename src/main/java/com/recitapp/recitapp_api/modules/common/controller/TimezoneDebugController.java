package com.recitapp.recitapp_api.modules.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@RestController
@RequestMapping("/debug")
@Slf4j
public class TimezoneDebugController {

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/timezone-test")
    public ResponseEntity<Map<String, Object>> testTimezone(@RequestBody TimezoneTestRequest request) {
        log.info("🕐 [TIMEZONE DEBUG] =================================");
        log.info("🕐 [TIMEZONE DEBUG] Received timezone test request");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Información del servidor
            TimeZone defaultTz = TimeZone.getDefault();
            ZoneId systemZone = ZoneId.systemDefault();
            
            log.info("🕐 [TIMEZONE DEBUG] Server Default Timezone: {}", defaultTz.getID());
            log.info("🕐 [TIMEZONE DEBUG] Server System Zone: {}", systemZone);
            log.info("🕐 [TIMEZONE DEBUG] ObjectMapper SerializationConfig: {}", objectMapper.getSerializationConfig().toString());
            
            // Datos recibidos
            log.info("🕐 [TIMEZONE DEBUG] Received startDateTime: {}", request.getStartDateTime());
            log.info("🕐 [TIMEZONE DEBUG] Received endDateTime: {}", request.getEndDateTime());
            
            // Analizar las fechas recibidas
            LocalDateTime start = request.getStartDateTime();
            LocalDateTime end = request.getEndDateTime();
            
            log.info("🕐 [TIMEZONE DEBUG] Parsed startDateTime: {}", start);
            log.info("🕐 [TIMEZONE DEBUG] Parsed endDateTime: {}", end);
            
            // Convertir a diferentes zonas horarias para ver el efecto
            ZonedDateTime startUTC = start.atZone(ZoneId.of("UTC"));
            ZonedDateTime startArgentina = start.atZone(ZoneId.of("America/Argentina/Buenos_Aires"));
            ZonedDateTime endUTC = end != null ? end.atZone(ZoneId.of("UTC")) : null;
            ZonedDateTime endArgentina = end != null ? end.atZone(ZoneId.of("America/Argentina/Buenos_Aires")) : null;
            
            log.info("🕐 [TIMEZONE DEBUG] Start as UTC: {}", startUTC);
            log.info("🕐 [TIMEZONE DEBUG] Start as Argentina: {}", startArgentina);
            if (end != null) {
                log.info("🕐 [TIMEZONE DEBUG] End as UTC: {}", endUTC);
                log.info("🕐 [TIMEZONE DEBUG] End as Argentina: {}", endArgentina);
            }
            
            // Respuesta
            response.put("received", Map.of(
                "startDateTime", request.getStartDateTime().toString(),
                "endDateTime", request.getEndDateTime() != null ? request.getEndDateTime().toString() : null
            ));
            
            response.put("serverInfo", Map.of(
                "defaultTimezone", defaultTz.getID(),
                "systemZone", systemZone.toString(),
                "objectMapperConfig", objectMapper.getSerializationConfig().toString(),
                "currentServerTime", LocalDateTime.now().toString()
            ));
            
            response.put("interpretations", Map.of(
                "asUTC", Map.of(
                    "start", startUTC.toString(),
                    "end", endUTC != null ? endUTC.toString() : null
                ),
                "asArgentina", Map.of(
                    "start", startArgentina.toString(),
                    "end", endArgentina != null ? endArgentina.toString() : null
                )
            ));
            
            // Simular lo que haría la base de datos
            response.put("databaseWouldStore", Map.of(
                "startDateTime", start.toString(),
                "endDateTime", end != null ? end.toString() : null,
                "note", "LocalDateTime se almacena sin zona horaria en la BD"
            ));
            
            log.info("🕐 [TIMEZONE DEBUG] =================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("🕐 [TIMEZONE DEBUG] Error: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    public static class TimezoneTestRequest {
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        
        // Getters y setters
        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }
        
        public void setStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }
        
        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }
        
        public void setEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }
    }
} 