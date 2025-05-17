package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.common.util.QRGenerator;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.entity.Promotion;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.repository.EventStatusRepository;
import com.recitapp.recitapp_api.modules.event.repository.PromotionRepository;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketAssignmentDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketPurchaseResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketStatusRepository;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.modules.transaction.entity.PaymentMethod;
import com.recitapp.recitapp_api.modules.transaction.entity.Transaction;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionDetail;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionStatus;
import com.recitapp.recitapp_api.modules.transaction.repository.PaymentMethodRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionDetailRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionStatusRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import com.recitapp.recitapp_api.modules.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final EventRepository eventRepository;
    private final EventStatusRepository eventStatusRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final TransactionStatusRepository transactionStatusRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PromotionRepository promotionRepository;
    private final QRGenerator qrGenerator;

    @Override
    @Transactional
    public TicketPurchaseResponseDTO purchaseTickets(TicketPurchaseRequestDTO purchaseRequest) {
        // Validate event exists
        Event event = eventRepository.findById(purchaseRequest.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + purchaseRequest.getEventId()));

        // Check if event is available for purchase
        if (!event.getStatus().getName().equals("EN_VENTA")) {
            throw new RecitappException("Las entradas para este evento no están disponibles para la compra");
        }

        // Validate user exists
        User user = userRepository.findById(purchaseRequest.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + purchaseRequest.getUserId()));

        // Validate payment method exists
        PaymentMethod paymentMethod = paymentMethodRepository.findById(purchaseRequest.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with ID: " +
                        purchaseRequest.getPaymentMethodId()));

        // Get "VENDIDA" status for tickets
        TicketStatus soldStatus = ticketStatusRepository.findByName("VENDIDA")
                .orElseThrow(() -> new EntityNotFoundException("Ticket status 'VENDIDA' not found"));

        // Get "COMPLETADA" status for transaction
        TransactionStatus completedStatus = transactionStatusRepository.findByName("COMPLETADA")
                .orElseThrow(() -> new EntityNotFoundException("Transaction status 'COMPLETADA' not found"));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus(completedStatus);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setIsRefund(false);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Ticket> tickets = new ArrayList<>();

        // Process each ticket
        for (TicketPurchaseRequestDTO.TicketRequestDTO ticketRequest : purchaseRequest.getTickets()) {
            // Validate section exists
            VenueSection section = venueSectionRepository.findById(ticketRequest.getSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " +
                            ticketRequest.getSectionId()));

            // Check if section belongs to the event's venue
            if (!section.getVenue().getId().equals(event.getVenue().getId())) {
                throw new RecitappException("La sección no pertenece al recinto del evento");
            }

            // Check availability for the section
            long availableTickets = countAvailableTicketsByEventAndSection(event.getId(), section.getId());
            if (availableTickets <= 0) {
                throw new RecitappException("No hay entradas disponibles para la sección: " + section.getName());
            }

            // Create ticket
            Ticket ticket = new Ticket();
            ticket.setEvent(event);
            ticket.setSection(section);
            ticket.setStatus(soldStatus);
            ticket.setSalePrice(ticketRequest.getPrice());
            ticket.setIdentificationCode(generateUniqueTicketCode());
            ticket.setUser(user);
            ticket.setAssignedUserFirstName(ticketRequest.getAttendeeFirstName());
            ticket.setAssignedUserLastName(ticketRequest.getAttendeeLastName());
            ticket.setAssignedUserDni(ticketRequest.getAttendeeDni());
            ticket.setPurchaseDate(LocalDateTime.now());
            ticket.setIsGift(false);
            ticket.setRegistrationDate(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            // Check if there's a promotion applied
            if (ticketRequest.getPromotionId() != null) {
                Promotion promotion = promotionRepository.findById(ticketRequest.getPromotionId())
                        .orElseThrow(() -> new EntityNotFoundException("Promotion not found with ID: " +
                                ticketRequest.getPromotionId()));
                ticket.setPromotion(promotion);
            }

            // Generate QR code
            String qrCode = generateQRCode(ticket);
            ticket.setQrCode(qrCode);

            tickets.add(ticket);
            totalAmount = totalAmount.add(ticketRequest.getPrice());
        }

        // Set transaction total amount
        transaction.setTotalAmount(totalAmount);
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Save tickets
        List<Ticket> savedTickets = ticketRepository.saveAll(tickets);

        // Create transaction details
        List<TransactionDetail> details = new ArrayList<>();
        for (Ticket ticket : savedTickets) {
            TransactionDetail detail = new TransactionDetail();
            TransactionDetail.TransactionDetailId detailId = new TransactionDetail.TransactionDetailId();
            detailId.setTransactionId(savedTransaction.getId());
            detailId.setTicketId(ticket.getId());
            detail.setId(detailId);
            detail.setTransaction(savedTransaction);
            detail.setTicket(ticket);
            detail.setUnitPrice(ticket.getSalePrice());
            details.add(detail);
        }
        transactionDetailRepository.saveAll(details);

        // Check if event should be marked as AGOTADO
        checkAndUpdateEventStatus(event.getId());

        // Map to response DTO
        return TicketPurchaseResponseDTO.builder()
                .transactionId(savedTransaction.getId())
                .purchaseDate(savedTransaction.getTransactionDate())
                .totalAmount(savedTransaction.getTotalAmount())
                .paymentMethod(paymentMethod.getName())
                .transactionStatus(completedStatus.getName())
                .tickets(savedTickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList()))
                .build();
    }

    @Override
    public TicketDTO getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));
        return mapToTicketDTO(ticket);
    }

    @Override
    public List<TicketDTO> getTicketsByEventId(Long eventId) {
        List<Ticket> tickets = ticketRepository.findByEventId(eventId);
        return tickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByUserId(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        return tickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList());
    }

    @Override
    public List<TicketDTO> getTicketsByEventAndSection(Long eventId, Long sectionId) {
        List<Ticket> tickets = ticketRepository.findByEventIdAndSectionId(eventId, sectionId);
        return tickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        TicketStatus canceledStatus = ticketStatusRepository.findByName("CANCELADA")
                .orElseThrow(() -> new EntityNotFoundException("Ticket status 'CANCELADA' not found"));

        ticket.setStatus(canceledStatus);
        ticketRepository.save(ticket);
    }

    @Override
    public boolean validateTicket(Long ticketId, String qrCode) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        return ticket.getQrCode().equals(qrCode) && ticket.getStatus().getName().equals("VENDIDA");
    }

    @Override
    @Transactional
    public TicketDTO transferTicket(Long ticketId, Long newUserId, String attendeeFirstName,
                                    String attendeeLastName, String attendeeDni) {
        // Validate ticket exists
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        // Validate new user exists
        User newUser = userRepository.findById(newUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + newUserId));

        // Validate ticket status
        if (!ticket.getStatus().getName().equals("VENDIDA")) {
            throw new RecitappException("Solo se pueden transferir entradas con estado VENDIDA");
        }

        // Validate the event has not passed
        if (ticket.getEvent().getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new RecitappException("No se pueden transferir entradas para eventos ya realizados");
        }

        // Update ticket information
        ticket.setUser(newUser);
        ticket.setAssignedUserFirstName(attendeeFirstName);
        ticket.setAssignedUserLastName(attendeeLastName);
        ticket.setAssignedUserDni(attendeeDni);
        ticket.setUpdatedAt(LocalDateTime.now());

        // Generate new QR code
        String newQrCode = generateQRCode(ticket);
        ticket.setQrCode(newQrCode);

        Ticket updatedTicket = ticketRepository.save(ticket);

        return mapToTicketDTO(updatedTicket);
    }

    @Override
    public String generateTicketQR(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        return generateQRCode(ticket);
    }

    @Override
    public Long countAvailableTicketsByEventAndSection(Long eventId, Long sectionId) {
        // Get the venue section to check its capacity
        VenueSection section = venueSectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " + sectionId));

        // Count sold tickets for this event and section
        long soldTickets = ticketRepository.countByEventIdAndSectionIdAndStatusName(eventId, sectionId, "VENDIDA");

        // Calculate available tickets
        return section.getCapacity() - soldTickets;
    }

    @Transactional
    public TicketDTO updateTicketAssignment(Long ticketId, TicketAssignmentDTO assignmentDTO) {
        // Retrieve the ticket by ID
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        // Check if the ticket can be modified
        validateTicketModification(ticket);

        // Update attendee information
        ticket.setAssignedUserFirstName(assignmentDTO.getAttendeeFirstName());
        ticket.setAssignedUserLastName(assignmentDTO.getAttendeeLastName());
        ticket.setAssignedUserDni(assignmentDTO.getAttendeeDni());
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

        // Save and return the updated ticket
        Ticket updatedTicket = ticketRepository.save(ticket);

        return mapToTicketDTO(updatedTicket);
    }

    /**
     * Validates if a ticket can be modified
     *
     * @param ticket The ticket to validate
     * @throws RecitappException If the ticket cannot be modified
     */
    private void validateTicketModification(Ticket ticket) {
        // Only tickets with "VENDIDA" status can be modified
        if (!ticket.getStatus().getName().equals("VENDIDA")) {
            throw new RecitappException("Solo se pueden modificar entradas con estado VENDIDA");
        }

        // Check if the event has already passed
        if (ticket.getEvent().getStartDateTime().isBefore(java.time.LocalDateTime.now())) {
            throw new RecitappException("No se pueden modificar entradas para eventos ya realizados");
        }

        // Check if the event is canceled
        if (ticket.getEvent().getStatus().getName().equals("CANCELADO")) {
            throw new RecitappException("No se pueden modificar entradas para eventos cancelados");
        }
    }

    // Helper methods
    private String generateUniqueTicketCode() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateQRCode(Ticket ticket) {
        // In a real implementation, we would use QRGenerator to generate a QR code image
        // For now, we'll just return a base64 placeholder string
        return "data:image/png;base64,QRCode-" + ticket.getIdentificationCode();
    }

    private TicketDTO mapToTicketDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .eventId(ticket.getEvent().getId())
                .eventName(ticket.getEvent().getName())
                .eventDate(ticket.getEvent().getStartDateTime())
                .sectionId(ticket.getSection().getId())
                .sectionName(ticket.getSection().getName())
                .venueName(ticket.getEvent().getVenue().getName())
                .price(ticket.getSalePrice())
                .status(ticket.getStatus().getName())
                .attendeeFirstName(ticket.getAssignedUserFirstName())
                .attendeeLastName(ticket.getAssignedUserLastName())
                .attendeeDni(ticket.getAssignedUserDni())
                .qrCode(ticket.getQrCode())
                .purchaseDate(ticket.getPurchaseDate())
                .build();
    }

    private void checkAndUpdateEventStatus(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Get the venue's total capacity
        Integer totalCapacity = event.getVenue().getTotalCapacity();

        // Count sold tickets for this event
        Long soldTickets = ticketRepository.countSoldTicketsByEventId(eventId);

        // If all tickets are sold, update event status to "AGOTADO"
        if (soldTickets >= totalCapacity) {
            // Fix: Use EventStatusRepository instead of EventRepository
            eventStatusRepository.findByName("AGOTADO")
                    .ifPresent(event::setStatus);
            eventRepository.save(event);
        }
    }
}