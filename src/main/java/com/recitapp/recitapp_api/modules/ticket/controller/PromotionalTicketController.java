package com.recitapp.recitapp_api.modules.ticket.controller;


import com.recitapp.recitapp_api.modules.ticket.dto.PromotionalTicketRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.PromotionalTicketResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.service.impl.PromotionalTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for promotional tickets
 */
@RestController
@RequestMapping("/admin/tickets/promotional")
@RequiredArgsConstructor

public class PromotionalTicketController {

    private final PromotionalTicketService promotionalTicketService;

    /**
     * Creates promotional tickets for an event
     *
     * @param requestDTO The request with promotional ticket details
     * @return Details of the created promotional tickets
     */
    @PostMapping
    public ResponseEntity<PromotionalTicketResponseDTO> createPromotionalTickets(
            @Valid @RequestBody PromotionalTicketRequestDTO requestDTO) {

        PromotionalTicketResponseDTO response = promotionalTicketService.createPromotionalTickets(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}