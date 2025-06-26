package com.recitapp.recitapp_api.modules.ticket.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketVerificationRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketVerificationResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketVerification;
import com.recitapp.recitapp_api.modules.ticket.service.impl.TicketVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ticket verification and QR validation
 */
@Slf4j
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
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO", "VERIFICADOR_ENTRADAS"})
    public ResponseEntity<TicketVerificationResponseDTO> verifyTicket(
            @Valid @RequestBody TicketVerificationRequestDTO requestDTO) {

        // Log access for debugging VERIFICADOR_ENTRADAS
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            log.info("🎫 Ticket verification accessed by user: {} (VERIFICADOR_ENTRADAS functionality)", userDetails.getUsername());
        }

        TicketVerificationResponseDTO response = verificationService.verifyTicket(requestDTO);
        log.info("✅ Ticket verification completed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Gets verification history for a ticket
     *
     * @param ticketId The ID of the ticket
     * @return A list of verification records
     */
    @GetMapping("/history/{ticketId}")
    @RequireRole({"ADMIN", "MODERADOR", "REGISTRADOR_EVENTO", "VERIFICADOR_ENTRADAS"})
    public ResponseEntity<List<TicketVerification>> getTicketVerificationHistory(
            @PathVariable Long ticketId) {

        List<TicketVerification> history = verificationService.getTicketVerificationHistory(ticketId);
        return ResponseEntity.ok(history);
    }
}