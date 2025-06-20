package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketStatusRepository;
import com.recitapp.recitapp_api.modules.ticket.service.TicketAdminService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketAdminServiceImpl implements TicketAdminService {

    private final TicketRepository ticketRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final TicketService ticketService;

    @Override
    @Transactional
    public Map<String, Object> markExpiredTickets() {
        log.info("Iniciando proceso de marcado de tickets vencidos");
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int processedCount = 0;
        int errorCount = 0;

        try {
            // Obtener estado VENCIDA, crearlo si no existe
            TicketStatus expiredStatus = getOrCreateExpiredStatus();
            
            // Obtener tickets de eventos pasados que están en estado VENDIDA
            LocalDateTime now = LocalDateTime.now();
            List<Ticket> ticketsToExpire = ticketRepository.findExpiredSoldTickets(now);
            
            log.info("Encontrados {} tickets para marcar como vencidos", ticketsToExpire.size());
            
            for (Ticket ticket : ticketsToExpire) {
                try {
                    ticket.setStatus(expiredStatus);
                    ticket.setUpdatedAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                    processedCount++;
                    
                    log.debug("Ticket {} marcado como vencido para evento {}", 
                            ticket.getId(), ticket.getEvent().getId());
                    
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("Error marcando ticket %d como vencido: %s", 
                            ticket.getId(), e.getMessage());
                    errors.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            result.put("success", true);
            result.put("totalFound", ticketsToExpire.size());
            result.put("processed", processedCount);
            result.put("errors", errorCount);
            result.put("errorDetails", errors);
            result.put("message", String.format("Proceso completado. %d tickets marcados como vencidos, %d errores", 
                    processedCount, errorCount));

            log.info("Proceso completado: {} tickets procesados, {} errores", processedCount, errorCount);

        } catch (Exception e) {
            log.error("Error durante el proceso de marcado de tickets vencidos: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("processed", processedCount);
            result.put("errors", errorCount + 1);
            result.put("message", "Error durante el proceso: " + e.getMessage());
            errors.add("Error general: " + e.getMessage());
            result.put("errorDetails", errors);
        }

        return result;
    }

    @Override
    public List<TicketDTO> getTicketsToExpire() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Ticket> ticketsToExpire = ticketRepository.findExpiredSoldTickets(now);
            
            List<TicketDTO> result = new ArrayList<>();
            for (Ticket ticket : ticketsToExpire) {
                result.add(convertTicketToDTO(ticket));
            }
            return result;
                    
        } catch (Exception e) {
            log.error("Error obteniendo preview de tickets a vencer: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> getTicketStatistics() {
        try {
            List<Object[]> stats = ticketRepository.getTicketStatisticsByStatus();
            
            Map<String, Long> result = new HashMap<>();
            for (Object[] stat : stats) {
                String statusName = (String) stat[0];
                Long count = (Long) stat[1];
                result.put(statusName, count);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de tickets: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    @Transactional
    public Map<String, Object> markSpecificTicketsExpired(List<Long> ticketIds) {
        log.info("Marcando {} tickets específicos como vencidos", ticketIds.size());
        
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int processedCount = 0;
        int errorCount = 0;

        try {
            TicketStatus expiredStatus = getOrCreateExpiredStatus();
            
            for (Long ticketId : ticketIds) {
                try {
                    Ticket ticket = ticketRepository.findById(ticketId)
                            .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));
                    
                    // Verificar que el ticket esté en estado VENDIDA
                    if (!"VENDIDA".equals(ticket.getStatus().getName())) {
                        errors.add(String.format("Ticket %d no está en estado VENDIDA (estado actual: %s)", 
                                ticketId, ticket.getStatus().getName()));
                        errorCount++;
                        continue;
                    }
                    
                    ticket.setStatus(expiredStatus);
                    ticket.setUpdatedAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                    processedCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = String.format("Error marcando ticket %d como vencido: %s", 
                            ticketId, e.getMessage());
                    errors.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            result.put("success", errorCount == 0);
            result.put("totalRequested", ticketIds.size());
            result.put("processed", processedCount);
            result.put("errors", errorCount);
            result.put("errorDetails", errors);
            result.put("message", String.format("Proceso completado. %d tickets marcados como vencidos, %d errores", 
                    processedCount, errorCount));

        } catch (Exception e) {
            log.error("Error durante el marcado manual de tickets: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("processed", processedCount);
            result.put("errors", errorCount + 1);
            result.put("message", "Error durante el proceso: " + e.getMessage());
            errors.add("Error general: " + e.getMessage());
            result.put("errorDetails", errors);
        }

        return result;
    }

    /**
     * Obtiene o crea el estado VENCIDA
     */
    private TicketStatus getOrCreateExpiredStatus() {
        Optional<TicketStatus> expiredStatus = ticketStatusRepository.findByName("VENCIDA");
        
        if (expiredStatus.isPresent()) {
            return expiredStatus.get();
        }
        
        // Crear el estado VENCIDA si no existe
        TicketStatus newExpiredStatus = new TicketStatus();
        newExpiredStatus.setName("VENCIDA");
        newExpiredStatus.setDescription("Ticket vencido - evento ya pasó");
        
        log.info("Creando nuevo estado de ticket: VENCIDA");
        return ticketStatusRepository.save(newExpiredStatus);
    }

    /**
     * Convierte un Ticket a TicketDTO completo con información del usuario y evento
     */
    private TicketDTO convertTicketToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        
        // Información básica del ticket
        dto.setId(ticket.getId());
        dto.setStatus(ticket.getStatus().getName());
        dto.setQrCode(ticket.getQrCode());
        dto.setPurchaseDate(ticket.getPurchaseDate());
        dto.setPrice(ticket.getSalePrice());
        
        // Información del evento
        if (ticket.getEvent() != null) {
            dto.setEventId(ticket.getEvent().getId());
            dto.setEventName(ticket.getEvent().getName());
            dto.setEventDate(ticket.getEvent().getStartDateTime());
            
            // Información del recinto
            if (ticket.getEvent().getVenue() != null) {
                dto.setVenueName(ticket.getEvent().getVenue().getName());
            }
        }
        
        // Información de la sección
        if (ticket.getSection() != null) {
            dto.setSectionId(ticket.getSection().getId());
            dto.setSectionName(ticket.getSection().getName());
        }
        
        // Información del usuario comprador
        if (ticket.getUser() != null) {
            dto.setUserId(ticket.getUser().getId());
            dto.setUserEmail(ticket.getUser().getEmail());
            dto.setUserFirstName(ticket.getUser().getFirstName());
            dto.setUserLastName(ticket.getUser().getLastName());
            dto.setUserName(ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName());
        }
        
        // Información del asistente (puede ser diferente al comprador)
        dto.setAttendeeFirstName(ticket.getAssignedUserFirstName());
        dto.setAttendeeLastName(ticket.getAssignedUserLastName());
        dto.setAttendeeDni(ticket.getAssignedUserDni());
        
        // Información promocional
        dto.setIsGift(ticket.getIsGift());
        dto.setPromotionName(ticket.getPromotion() != null ? ticket.getPromotion().getName() : null);
        dto.setPromotionDescription(ticket.getPromotion() != null ? ticket.getPromotion().getDescription() : null);
        dto.setTicketType(determineTicketType(ticket));
        
        return dto;
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