package com.recitapp.recitapp_api.modules.ticket.service;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketAssignmentDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;

import java.util.List;

public interface TicketService {
    TicketPurchaseResponseDTO purchaseTickets(TicketPurchaseRequestDTO purchaseRequest);

    TicketDTO getTicketById(Long ticketId);

    List<TicketDTO> getTicketsByEventId(Long eventId);

    List<TicketDTO> getTicketsByUserId(Long userId);

    List<TicketDTO> getTicketsByEventAndSection(Long eventId, Long sectionId);

    void cancelTicket(Long ticketId);

    boolean validateTicket(Long ticketId, String qrCode);

    TicketDTO transferTicket(Long ticketId, Long newUserId, String attendeeFirstName,
                             String attendeeLastName, String attendeeDni);

    String generateTicketQR(Long ticketId);

    Long countAvailableTicketsByEventAndSection(Long eventId, Long sectionId);

    TicketDTO updateTicketAssignment(Long ticketId, TicketAssignmentDTO assignmentDTO);

}