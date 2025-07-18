package com.recitapp.recitapp_api.modules.ticket.service;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketAssignmentDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
    TicketPurchaseResponseDTO purchaseTickets(TicketPurchaseRequestDTO purchaseRequest);

    TicketDTO getTicketById(Long ticketId);

    List<TicketDTO> getTicketsByEventId(Long eventId);

    List<TicketDTO> getTicketsByUserId(Long userId);

    List<TicketDTO> getTicketsByEventAndSection(Long eventId, Long sectionId);

    void cancelTicket(Long ticketId);

    boolean validateTicket(Long ticketId, String qrCode);

    boolean validateTicketByCode(String identificationCode);

    TicketDTO transferTicket(Long ticketId, Long newUserId, String attendeeFirstName,
                             String attendeeLastName, String attendeeDni);

    String generateTicketQR(Long ticketId);

    Long countAvailableTicketsByEventAndSection(Long eventId, Long sectionId);

    TicketDTO updateTicketAssignment(Long ticketId, TicketAssignmentDTO assignmentDTO);

    TicketDTO transferTicketBySearch(Long userId, Long ticketId,
                                     String recipientFirstName,
                                     String recipientLastName,
                                     String recipientDni);

    /**
     * Get paginated tickets for a specific user
     */
    Page<TicketDTO> getUserTicketsPaginated(Long userId, Pageable pageable);
}