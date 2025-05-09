package com.recitapp.recitapp_api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase para representar una respuesta de error estandarizada en la API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    /**
     * Fecha y hora en que se produjo el error
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP
     */
    private int status;

    /**
     * Tipo de error general
     */
    private String error;

    /**
     * Mensaje descriptivo del error
     */
    private String message;

    /**
     * Detalles adicionales del error (opcional)
     */
    private Object details;

    /**
     * Ruta en la que se produjo el error
     */
    private String path;
}