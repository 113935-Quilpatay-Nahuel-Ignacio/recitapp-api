package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.common.util.QRGenerator;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.entity.Promotion;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.repository.PromotionRepository;
import com.recitapp.recitapp_api.modules.ticket.dto.PromotionalTicketRequestDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.PromotionalTicketResponseDTO;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketStatusRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import com.recitapp.recitapp_api.modules.venue.repository.VenueSectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing promotional tickets
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionalTicketService {

    private final TicketRepository ticketRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final PromotionRepository promotionRepository;
    private final QRGenerator qrGenerator;

    /**
     * Creates promotional tickets for an event
     *
     * @param requestDTO The request containing promotional ticket details
     * @return A response with details about the created tickets
     */
    @Transactional
    public PromotionalTicketResponseDTO createPromotionalTickets(PromotionalTicketRequestDTO requestDTO) {
        // Validate event exists
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + requestDTO.getEventId()));

        // Validate admin user exists and has correct permissions
        User adminUser = userRepository.findById(requestDTO.getAdminUserId())
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found with ID: " + requestDTO.getAdminUserId()));

        // Validate admin user has appropriate role
        if (!hasAdminRole(adminUser)) {
            throw new RecitappException("User does not have permission to create promotional tickets");
        }

        // Check if the event is valid for promotional tickets
        validateEventForPromotionalTickets(event);

        // Get "REGALO" or "VENDIDA" status for tickets
        TicketStatus ticketStatus = ticketStatusRepository.findByName("REGALO")
                .orElseGet(() -> ticketStatusRepository.findByName("VENDIDA")
                        .orElseThrow(() -> new EntityNotFoundException("Required ticket status not found")));

        // Create a list to store the created tickets
        List<Ticket> createdTickets = new ArrayList<>();

        // Create a promotion record if name and description are provided
        Promotion promotion = createPromotionIfNeeded(event, requestDTO);

        // Process each ticket
        for (PromotionalTicketRequestDTO.PromotionalTicketDTO ticketDTO : requestDTO.getTickets()) {
            // Validate section exists
            VenueSection section = venueSectionRepository.findById(ticketDTO.getSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " + ticketDTO.getSectionId()));

            // Validate recipient user exists
            User recipientUser = userRepository.findById(ticketDTO.getRecipientUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Recipient user not found with ID: " + ticketDTO.getRecipientUserId()));

            // Check availability for the section
            validateSectionAvailability(event.getId(), section.getId());

            // Create the ticket
            Ticket ticket = createTicket(ticketDTO, event, section, ticketStatus, recipientUser, promotion);
            createdTickets.add(ticket);
        }

        // Save all tickets
        List<Ticket> savedTickets = ticketRepository.saveAll(createdTickets);

        // Build the response
        return buildPromotionalTicketResponse(requestDTO, event, adminUser, savedTickets);
    }

    /**
     * Creates a ticket entity from the provided details
     */
    private Ticket createTicket(PromotionalTicketRequestDTO.PromotionalTicketDTO ticketDTO,
                                Event event, VenueSection section, TicketStatus ticketStatus,
                                User recipientUser, Promotion promotion) {

        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setSection(section);
        ticket.setStatus(ticketStatus);
        ticket.setSalePrice(BigDecimal.ZERO);
        ticket.setIdentificationCode(generateUniqueTicketCode());
        ticket.setUser(recipientUser);
        ticket.setAssignedUserFirstName(ticketDTO.getAttendeeFirstName());
        ticket.setAssignedUserLastName(ticketDTO.getAttendeeLastName());
        ticket.setAssignedUserDni(ticketDTO.getAttendeeDni());
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setIsGift(ticketDTO.isGift());
        ticket.setRegistrationDate(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        if (promotion != null) {
            ticket.setPromotion(promotion);
        }

        // Generate QR code - now with proper error handling
        String verificationCode = UUID.randomUUID().toString().substring(0, 12);
        String qrCode = qrGenerator.generateTicketQR(null, verificationCode);
        ticket.setQrCode(qrCode);

        return ticket;
    }

    /**
     * Creates a promotion if name and description are provided
     */
    private Promotion createPromotionIfNeeded(Event event, PromotionalTicketRequestDTO requestDTO) {
        if (requestDTO.getPromotionName() == null || requestDTO.getPromotionName().isBlank()) {
            return null;
        }

        Promotion promotion = new Promotion();
        promotion.setEvent(event);
        promotion.setName(requestDTO.getPromotionName());
        promotion.setDescription(requestDTO.getPromotionDescription());
        promotion.setMinimumQuantity(1);
        promotion.setDiscountPercentage(BigDecimal.valueOf(100)); // 100% discount
        promotion.setApplyToTotal(true);
        promotion.setStartDate(LocalDateTime.now());
        promotion.setEndDate(event.getStartDateTime());
        promotion.setActive(true);
        promotion.setPromotionCode("PROMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        return promotionRepository.save(promotion);
    }

    /**
     * Builds the response for created promotional tickets
     */
    private PromotionalTicketResponseDTO buildPromotionalTicketResponse(
            PromotionalTicketRequestDTO requestDTO, Event event, User adminUser, List<Ticket> savedTickets) {

        return PromotionalTicketResponseDTO.builder()
                .eventId(event.getId())
                .eventName(event.getName())
                .promotionName(requestDTO.getPromotionName())
                .promotionDescription(requestDTO.getPromotionDescription())
                .creationDate(LocalDateTime.now())
                .adminUserId(adminUser.getId())
                .adminUserName(adminUser.getFirstName() + " " + adminUser.getLastName())
                .ticketCount(savedTickets.size())
                .tickets(savedTickets.stream().map(this::mapToTicketDTO).collect(Collectors.toList()))
                .build();
    }

    /**
     * Validates if the user has an admin role
     *
     * @param user The user to check
     * @return true if the user has admin permissions
     */
    private boolean hasAdminRole(User user) {
        String roleName = user.getRole().getName();
        return "ADMIN".equals(roleName) || "MODERADOR".equals(roleName) || "REGISTRADOR_EVENTO".equals(roleName);
    }

    /**
     * Validates if an event is valid for creating promotional tickets
     *
     * @param event The event to validate
     * @throws RecitappException If the event is not valid
     */
    private void validateEventForPromotionalTickets(Event event) {
        // Check if the event is canceled
        if ("CANCELADO".equals(event.getStatus().getName())) {
            throw new RecitappException("No se pueden crear entradas promocionales para eventos cancelados");
        }

        // Check if the event has already passed
        if (event.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new RecitappException("No se pueden crear entradas promocionales para eventos ya realizados");
        }

        // Check if the event is verified
        if (!event.getVerified()) {
            throw new RecitappException("No se pueden crear entradas promocionales para eventos no verificados");
        }
    }

    /**
     * Validates if there are available tickets in a section
     *
     * @param eventId The ID of the event
     * @param sectionId The ID of the section
     * @throws RecitappException If there are no available tickets
     */
    private void validateSectionAvailability(Long eventId, Long sectionId) {
        // Get the venue section to check its capacity
        VenueSection section = venueSectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with ID: " + sectionId));

        // Count sold tickets for this event and section
        long soldTickets = ticketRepository.countByEventIdAndSectionIdAndStatusName(eventId, sectionId, "VENDIDA");
        soldTickets += ticketRepository.countByEventIdAndSectionIdAndStatusName(eventId, sectionId, "REGALO");

        // Check if there are available tickets
        if (soldTickets >= section.getCapacity()) {
            throw new RecitappException("No hay entradas disponibles en la sección: " + section.getName());
        }
    }

    /**
     * Generates a unique code for a ticket
     *
     * @return A unique ticket code
     */
    private String generateUniqueTicketCode() {
        return "PROMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Maps a Ticket entity to a TicketDTO
     *
     * @param ticket The ticket entity to map
     * @return The corresponding DTO
     */
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
                .userId(ticket.getUser().getId())
                .userName(ticket.getUser().getEmail())
                .userEmail(ticket.getUser().getEmail())
                .userFirstName(ticket.getUser().getFirstName())
                .userLastName(ticket.getUser().getLastName())
                .isGift(ticket.getIsGift())
                .promotionName(ticket.getPromotion() != null ? ticket.getPromotion().getName() : null)
                .promotionDescription(ticket.getPromotion() != null ? ticket.getPromotion().getDescription() : null)
                .ticketType(determineTicketType(ticket))
                .build();
    }

    /**
     * Determines the ticket type based on promotion and gift status
     * @param ticket The ticket to analyze
     * @return The ticket type string
     */
    private String determineTicketType(Ticket ticket) {
        // Check if it's a gift ticket first
        if (ticket.getIsGift() != null && ticket.getIsGift()) {
            return "GIFT";
        }
        
        // Check if it has a promotion
        if (ticket.getPromotion() != null) {
            String promotionName = ticket.getPromotion().getName();
            String promotionDescription = ticket.getPromotion().getDescription();
            
            // Check for 2x1 promotion (case insensitive)
            boolean is2x1 = (promotionName != null && promotionName.toLowerCase().contains("2x1")) ||
                           (promotionDescription != null && promotionDescription.toLowerCase().contains("2x1")) ||
                           (promotionName != null && promotionName.toLowerCase().contains("dos por uno")) ||
                           (promotionDescription != null && promotionDescription.toLowerCase().contains("dos por uno"));
            
            if (is2x1) {
                return "PROMOTIONAL_2X1";
            } else {
                return "PROMOTIONAL";
            }
        }
        
        return "GENERAL";
    }
}