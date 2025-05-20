package com.recitapp.recitapp_api.modules.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketTransferBySearchDTO {
    @NotBlank(message = "El nombre del usuario receptor es obligatorio")
    private String recipientFirstName;

    @NotBlank(message = "El apellido del usuario receptor es obligatorio")
    private String recipientLastName;

    @NotBlank(message = "El DNI del usuario receptor es obligatorio")
    private String recipientDni;
}