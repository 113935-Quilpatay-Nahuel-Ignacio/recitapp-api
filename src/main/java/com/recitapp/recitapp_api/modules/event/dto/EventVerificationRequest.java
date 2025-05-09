package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitar la verificación de un evento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventVerificationRequest {

    /**
     * ID del moderador que realiza la verificación
     */
    @NotNull(message = "El ID del moderador es obligatorio")
    private Long moderatorId;

    /**
     * Comentarios opcionales sobre la verificación
     */
    private String verificationNotes;

    /**
     * Indica si se debe actualizar el estado del evento
     */
    private Boolean updateStatus;

    /**
     * Nuevo estado para el evento (si updateStatus es true)
     */
    private String newStatus;
}