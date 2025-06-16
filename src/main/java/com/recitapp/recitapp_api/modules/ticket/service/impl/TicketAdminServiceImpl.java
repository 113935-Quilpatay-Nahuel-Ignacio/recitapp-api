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
     * Convierte un Ticket a TicketDTO básico
     */
    private TicketDTO convertTicketToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setEventId(ticket.getEvent().getId());
        dto.setEventName(ticket.getEvent().getName());
        dto.setStatus(ticket.getStatus().getName());
        dto.setSectionName(ticket.getSection().getName());
        dto.setQrCode(ticket.getQrCode());
        dto.setPurchaseDate(ticket.getPurchaseDate());
        return dto;
    }
} 