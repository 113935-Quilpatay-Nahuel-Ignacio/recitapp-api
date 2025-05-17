package com.recitapp.recitapp_api.modules.ticket.controller;

import com.recitapp.recitapp_api.modules.ticket.dto.*;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/purchase")
    public ResponseEntity<TicketPurchaseResponseDTO> purchaseTickets(
            @Valid @RequestBody TicketPurchaseRequestDTO purchaseRequest) {
        TicketPurchaseResponseDTO response = ticketService.purchaseTickets(purchaseRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        TicketDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByEventId(@PathVariable Long eventId) {
        List<TicketDTO> tickets = ticketService.getTicketsByEventId(eventId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByUserId(@PathVariable Long userId) {
        List<TicketDTO> tickets = ticketService.getTicketsByUserId(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/event/{eventId}/section/{sectionId}")
    public ResponseEntity<List<TicketDTO>> getTicketsByEventAndSection(
            @PathVariable Long eventId, @PathVariable Long sectionId) {
        List<TicketDTO> tickets = ticketService.getTicketsByEventAndSection(eventId, sectionId);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelTicket(@PathVariable Long id) {
        ticketService.cancelTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<Boolean> validateTicket(
            @PathVariable Long id, @RequestParam String qrCode) {
        boolean isValid = ticketService.validateTicket(id, qrCode);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/{id}/generate-qr")
    public ResponseEntity<String> generateQRCode(@PathVariable Long id) {
        String qrCode = ticketService.generateTicketQR(id);
        return ResponseEntity.ok(qrCode);
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<TicketDTO> transferTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketTransferRequestDTO transferRequest) {
        TicketDTO transferredTicket = ticketService.transferTicket(
                id,
                transferRequest.getRecipientUserId(),
                transferRequest.getAttendeeFirstName(),
                transferRequest.getAttendeeLastName(),
                transferRequest.getAttendeeDni());
        return ResponseEntity.ok(transferredTicket);
    }

    @GetMapping("/event/{eventId}/section/{sectionId}/available-count")
    public ResponseEntity<Long> getAvailableTicketsCount(
            @PathVariable Long eventId, @PathVariable Long sectionId) {
        Long availableCount = ticketService.countAvailableTicketsByEventAndSection(eventId, sectionId);
        return ResponseEntity.ok(availableCount);
    }

    @PatchMapping("/{ticketId}/assignment")
    public ResponseEntity<TicketDTO> updateTicketAssignment(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketAssignmentDTO assignmentDTO) {

        TicketDTO updatedTicket = ticketService.updateTicketAssignment(ticketId, assignmentDTO);
        return ResponseEntity.ok(updatedTicket);
    }

}