package com.recitapp.recitapp_api.modules.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketPurchaseRequestDTO {
    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventId;

    @NotNull(message = "El ID del método de pago es obligatorio")
    private Long paymentMethodId;

    @NotNull(message = "El ID del usuario comprador es obligatorio")
    private Long userId;

    @NotEmpty(message = "Se debe incluir al menos un ticket")
    private List<TicketRequestDTO> tickets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketRequestDTO {
        @NotNull(message = "El ID de la sección es obligatorio")
        private Long sectionId;

        @NotNull(message = "El nombre del asistente es obligatorio")
        private String attendeeFirstName;

        @NotNull(message = "El apellido del asistente es obligatorio")
        private String attendeeLastName;

        @NotNull(message = "El DNI del asistente es obligatorio")
        private String attendeeDni;

        @NotNull(message = "El precio del ticket es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        private java.math.BigDecimal price;

        private Long promotionId;
    }
}