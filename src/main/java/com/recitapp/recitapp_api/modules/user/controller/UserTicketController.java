package com.recitapp.recitapp_api.modules.user.controller;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
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

    @PostMapping("/{ticketId}/transfer")
    public ResponseEntity<TicketDTO> transferTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketTransferRequestDTO transferRequest) {

        // Verify ticket belongs to the user
        // (In a real implementation, this would check the user's ownership)

        TicketDTO transferredTicket = ticketService.transferTicket(
                ticketId,
                transferRequest.getRecipientUserId(),
                transferRequest.getAttendeeFirstName(),
                transferRequest.getAttendeeLastName(),
                transferRequest.getAttendeeDni());

        return ResponseEntity.ok(transferredTicket);
    }
}