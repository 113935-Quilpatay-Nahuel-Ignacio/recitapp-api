package com.recitapp.recitapp_api.modules.user.controller;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketTransferBySearchDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketTransferRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/tickets")
@RequiredArgsConstructor
public class UserTicketController {

    private final TicketService ticketService;

    /**
     * Endpoint original que acepta directamente el ID del usuario receptor
     */
    @PostMapping("/{ticketId}/transfer")
    public ResponseEntity<TicketDTO> transferTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketTransferRequestDTO transferRequest) {

        TicketDTO transferredTicket = ticketService.transferTicket(
                ticketId,
                transferRequest.getRecipientUserId(),
                transferRequest.getAttendeeFirstName(),
                transferRequest.getAttendeeLastName(),
                transferRequest.getAttendeeDni());

        return ResponseEntity.ok(transferredTicket);
    }

    /**
     * Endpoint simplificado que busca al usuario receptor por nombre, apellido y DNI
     * y asigna automáticamente esos datos como datos del asistente
     */
    @PostMapping("/{ticketId}/transfer-search")
    public ResponseEntity<TicketDTO> transferTicketBySearch(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketTransferBySearchDTO transferRequest) {

        TicketDTO transferredTicket = ticketService.transferTicketBySearch(
                userId,
                ticketId,
                transferRequest.getRecipientFirstName(),
                transferRequest.getRecipientLastName(),
                transferRequest.getRecipientDni());

        return ResponseEntity.ok(transferredTicket);
    }
}