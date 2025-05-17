package com.recitapp.recitapp_api.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ticket assignment modification requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentDTO {
    @NotBlank(message = "El nombre del asistente es obligatorio")
    private String attendeeFirstName;

    @NotBlank(message = "El apellido del asistente es obligatorio")
    private String attendeeLastName;

    @NotBlank(message = "El DNI del asistente es obligatorio")
    private String attendeeDni;
}