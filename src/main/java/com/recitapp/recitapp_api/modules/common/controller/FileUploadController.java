package com.recitapp.recitapp_api.modules.common.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/event-flyer")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Map<String, Object>> uploadEventFlyer(@RequestParam("file") MultipartFile file) {
        log.info("📤 [UPLOAD CONTROLLER] Received event flyer upload request");
        log.info("📤 [UPLOAD CONTROLLER] File details - name: '{}', size: {} bytes, type: '{}'", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        try {
            log.info("📤 [UPLOAD CONTROLLER] Calling fileStorageService.storeImage...");
            String fileUrl = fileStorageService.storeImage(file, "event-flyers");
            log.info("📤 [UPLOAD CONTROLLER] File stored successfully with URL: '{}'", fileUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("message", "Imagen del evento subida correctamente");
            
            log.info("📤 [UPLOAD CONTROLLER] Returning success response");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("📤 [UPLOAD CONTROLLER] Error uploading event flyer: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload/event-sections")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Map<String, Object>> uploadEventSections(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeImage(file, "event-sections");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("message", "Imagen de secciones subida correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al subir imagen de secciones: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/validate-image")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Map<String, Object>> validateImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = fileStorageService.isValidImage(file);
        response.put("valid", isValid);
        
        if (isValid) {
            response.put("message", "Imagen válida");
            response.put("size", file.getSize());
            response.put("type", file.getContentType());
        } else {
            response.put("message", "Imagen no válida o no cumple con los requisitos");
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/artist-profile")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> uploadArtistProfile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeImage(file, "artist-profiles");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("message", "Imagen de perfil del artista subida correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al subir imagen de perfil del artista: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload/venue-image")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> uploadVenueImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeImage(file, "venue-images");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("message", "Imagen del recinto subida correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al subir imagen del recinto: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload/notification-image")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> uploadNotificationImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeImage(file, "notification-images");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("message", "Imagen de notificación subida correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al subir imagen de notificación: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/upload/user-profile")
    @RequireRole({"ADMIN", "USER"})
    public ResponseEntity<Map<String, Object>> uploadUserProfile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageService.storeImage(file, "user-profiles");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fileUrl", fileUrl);
            response.put("message", "Imagen de perfil subida correctamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al subir imagen de perfil: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al subir la imagen: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            boolean deleted = fileStorageService.deleteFile(fileUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            
            if (deleted) {
                response.put("message", "Archivo eliminado correctamente");
            } else {
                response.put("message", "No se pudo eliminar el archivo");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al eliminar archivo: {}", e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar el archivo: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 