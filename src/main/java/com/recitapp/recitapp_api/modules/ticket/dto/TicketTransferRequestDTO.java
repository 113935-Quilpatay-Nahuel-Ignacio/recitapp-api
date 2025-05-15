package com.recitapp.recitapp_api.modules.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketTransferRequestDTO {
    @NotNull(message = "El ID del usuario receptor es obligatorio")
    private Long recipientUserId;

    @NotBlank(message = "El nombre del asistente es obligatorio")
    private String attendeeFirstName;

    @NotBlank(message = "El apellido del asistente es obligatorio")
    private String attendeeLastName;

    @NotBlank(message = "El DNI del asistente es obligatorio")
    private String attendeeDni;
}