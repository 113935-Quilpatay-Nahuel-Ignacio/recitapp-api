package com.recitapp.recitapp_api.modules.ticket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating promotional tickets
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionalTicketRequestDTO {

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventId;

    @NotNull(message = "El ID del usuario administrador es obligatorio")
    private Long adminUserId;

    @NotEmpty(message = "Debe proporcionar al menos un ticket promocional")
    @Valid
    private List<PromotionalTicketDTO> tickets;

    private String promotionName;
    private String promotionDescription;

    /**
     * Individual promotional ticket details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionalTicketDTO {
        @NotNull(message = "El ID de la sección es obligatorio")
        private Long sectionId;

        @NotNull(message = "El ID del usuario receptor es obligatorio")
        private Long recipientUserId;

        @NotNull(message = "El nombre del asistente es obligatorio")
        private String attendeeFirstName;

        @NotNull(message = "El apellido del asistente es obligatorio")
        private String attendeeLastName;

        @NotNull(message = "El DNI del asistente es obligatorio")
        private String attendeeDni;

        private boolean isGift = true;
    }
}