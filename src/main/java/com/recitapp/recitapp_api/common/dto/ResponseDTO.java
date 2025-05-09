package com.recitapp.recitapp_api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Clase para representar una respuesta estandarizada en la API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {

    /**
     * Fecha y hora de la respuesta
     */
    private LocalDateTime timestamp;

    /**
     * Estado de la operación (true=éxito, false=error)
     */
    private boolean success;

    /**
     * Mensaje descriptivo
     */
    private String message;

    /**
     * Datos devueltos (si aplica)
     */
    private T data;

    /**
     * Construye una respuesta de éxito con datos
     *
     * @param data Los datos a devolver
     * @param message Mensaje descriptivo
     * @return Una respuesta de éxito con los datos
     */
    public static <T> ResponseDTO<T> success(T data, String message) {
        return ResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Construye una respuesta de éxito con datos
     *
     * @param data Los datos a devolver
     * @return Una respuesta de éxito con los datos
     */
    public static <T> ResponseDTO<T> success(T data) {
        return success(data, "Operación exitosa");
    }

    /**
     * Construye una respuesta de éxito sin datos
     *
     * @param message Mensaje descriptivo
     * @return Una respuesta de éxito sin datos
     */
    public static <T> ResponseDTO<T> success(String message) {
        return ResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Construye una respuesta de error
     *
     * @param message Mensaje de error
     * @return Una respuesta de error
     */
    public static <T> ResponseDTO<T> error(String message) {
        return ResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .success(false)
                .message(message)
                .build();
    }
}