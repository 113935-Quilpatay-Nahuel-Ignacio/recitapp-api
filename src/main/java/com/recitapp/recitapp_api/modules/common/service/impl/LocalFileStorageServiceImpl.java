package com.recitapp.recitapp_api.modules.common.service.impl;

import com.recitapp.recitapp_api.modules.common.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_WIDTH = 2000;
    private static final int MAX_HEIGHT = 2000;

    @Override
    public String storeImage(MultipartFile file, String category) {
        log.info("🔄 [UPLOAD DEBUG] Starting upload process for category: '{}'", category);
        log.info("🔄 [UPLOAD DEBUG] File info - name: '{}', size: {} bytes, contentType: '{}'", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        try {
            log.info("🔄 [UPLOAD DEBUG] Validating image...");
            if (!isValidImage(file)) {
                log.error("❌ [UPLOAD DEBUG] Image validation failed!");
                throw new RuntimeException("Archivo de imagen no válido");
            }
            log.info("✅ [UPLOAD DEBUG] Image validation passed");

            // Crear directorio si no existe
            Path categoryPath = Paths.get(uploadDir, category);
            log.info("🔄 [UPLOAD DEBUG] Creating directory: {}", categoryPath.toAbsolutePath());
            Files.createDirectories(categoryPath);
            log.info("✅ [UPLOAD DEBUG] Directory created/exists");

            // Generar nombre único para el archivo
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getFileExtension(originalFilename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String filename = String.format("%s_%s.%s", timestamp, uniqueId, extension);
            log.info("🔄 [UPLOAD DEBUG] Generated filename: '{}'", filename);

            // Guardar archivo
            Path targetPath = categoryPath.resolve(filename);
            log.info("🔄 [UPLOAD DEBUG] Saving file to: {}", targetPath.toAbsolutePath());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("✅ [UPLOAD DEBUG] File saved successfully");

            log.info("Archivo guardado: {} en categoría: {}", filename, category);
            log.info("🔄 [UPLOAD DEBUG] Generating file URL...");

            String fileUrl = getFileUrl(filename, category);
            log.info("✅ [UPLOAD DEBUG] Upload process completed successfully. Final URL: '{}'", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("❌ [UPLOAD DEBUG] IOException during upload: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el archivo", e);
        } catch (Exception e) {
            log.error("❌ [UPLOAD DEBUG] Unexpected error during upload: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado al guardar el archivo", e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
                return false;
            }

            // Extraer path relativo de la URL
            String relativePath = fileUrl.substring(baseUrl.length());
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = Paths.get(uploadDir, relativePath);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("Archivo eliminado: {}", filePath);
            }

            return deleted;

        } catch (IOException e) {
            log.error("Error al eliminar archivo: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Archivo demasiado grande: {} bytes", file.getSize());
            return false;
        }

        // Validar extensión
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Extensión no permitida: {}", extension);
            return false;
        }

        // Validar que realmente sea una imagen
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                log.warn("El archivo no es una imagen válida");
                return false;
            }

            // Validar dimensiones
            if (image.getWidth() > MAX_WIDTH || image.getHeight() > MAX_HEIGHT) {
                log.warn("Imagen demasiado grande: {}x{}", image.getWidth(), image.getHeight());
                return false;
            }

            return true;

        } catch (IOException e) {
            log.error("Error al validar imagen: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getFileUrl(String filename, String category) {
        // baseUrl now includes the context path from properties
        String generatedUrl = String.format("%s/%s/%s/%s", baseUrl, uploadDir, category, filename);
        log.info("🖼️ [FILE URL DEBUG] Generated URL: '{}'", generatedUrl);
        log.info("🖼️ [FILE URL DEBUG] baseUrl includes context: '{}', uploadDir: '{}', category: '{}', filename: '{}'", 
                baseUrl, uploadDir, category, filename);
        return generatedUrl;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
} 