package com.recitapp.recitapp_api.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for ticket verification requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketVerificationRequestDTO {

    @NotNull(message = "El ID del ticket es obligatorio")
    private Long ticketId;

    @NotBlank(message = "El código QR es obligatorio")
    private String qrCode;

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventId;

    @NotNull(message = "El ID del punto de acceso es obligatorio")
    private Long accessPointId;

    @NotNull(message = "El ID del usuario verificador es obligatorio")
    private Long verifierUserId;
}