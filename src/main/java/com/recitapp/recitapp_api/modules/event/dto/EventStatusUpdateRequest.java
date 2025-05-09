package com.recitapp.recitapp_api.modules.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitar la actualización del estado de un evento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusUpdateRequest {

    /**
     * Nuevo estado para el evento
     */
    @NotBlank(message = "El nombre del estado es obligatorio")
    private String statusName;

    /**
     * Comentarios opcionales sobre el cambio de estado
     */
    private String statusChangeNotes;
}