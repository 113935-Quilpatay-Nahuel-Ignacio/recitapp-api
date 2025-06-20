package com.recitapp.recitapp_api.modules.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Almacena un archivo de imagen y retorna la URL de acceso
     * @param file El archivo a almacenar
     * @param category Categoría del archivo (event-flyer, event-sections, etc.)
     * @return URL de acceso al archivo almacenado
     */
    String storeImage(MultipartFile file, String category);
    
    /**
     * Elimina un archivo del almacenamiento
     * @param fileUrl URL del archivo a eliminar
     * @return true si se eliminó correctamente
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * Valida si un archivo es una imagen válida
     * @param file El archivo a validar
     * @return true si es una imagen válida
     */
    boolean isValidImage(MultipartFile file);
    
    /**
     * Obtiene la URL completa para acceder a un archivo
     * @param filename Nombre del archivo
     * @param category Categoría del archivo
     * @return URL completa de acceso
     */
    String getFileUrl(String filename, String category);
} 