package com.recitapp.recitapp_api.modules.ticket.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketVerificationRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketVerificationResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketVerification;
import com.recitapp.recitapp_api.modules.ticket.service.impl.TicketVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ticket verification operations
 */
@RestController
@RequestMapping("/tickets/verification")
@RequiredArgsConstructor
public class TicketVerificationController {

    private final TicketVerificationService verificationService;

    /**
     * Verifies a ticket at an access point
     *
     * @param requestDTO The verification request
     * @return The verification result
     */
    @PostMapping("/verify")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<TicketVerificationResponseDTO> verifyTicket(
            @Valid @RequestBody TicketVerificationRequestDTO requestDTO) {

        TicketVerificationResponseDTO response = verificationService.verifyTicket(requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets verification history for a ticket
     *
     * @param ticketId The ID of the ticket
     * @return A list of verification records
     */
    @GetMapping("/history/{ticketId}")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO"})
    public ResponseEntity<List<TicketVerification>> getTicketVerificationHistory(
            @PathVariable Long ticketId) {

        List<TicketVerification> history = verificationService.getTicketVerificationHistory(ticketId);
        return ResponseEntity.ok(history);
    }
}