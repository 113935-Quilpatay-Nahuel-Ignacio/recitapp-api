package com.recitapp.recitapp_api.modules.ticket.controller;


import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para operaciones administrativas de tickets
 */
@RestController
@RequestMapping("/admin/tickets")
@RequiredArgsConstructor
public class TicketAdminController {

    private final TicketAdminService ticketAdminService;

    /**
     * Marcar tickets de eventos pasados como vencidos (cambiar estado de VENDIDA a VENCIDA)
     * Solo accesible para administradores
     * 
     * @return Información sobre los tickets procesados
     */
    @PostMapping("/mark-expired")
    public ResponseEntity<Map<String, Object>> markExpiredTickets() {
        Map<String, Object> result = ticketAdminService.markExpiredTickets();
        return ResponseEntity.ok(result);
    }

    /**
     * Obtener tickets que pueden ser marcados como vencidos (preview)
     * 
     * @return Lista de tickets que serían marcados como vencidos
     */
    @GetMapping("/preview-expired")
    public ResponseEntity<List<TicketDTO>> previewExpiredTickets() {
        List<TicketDTO> expiredTickets = ticketAdminService.getTicketsToExpire();
        return ResponseEntity.ok(expiredTickets);
    }

    /**
     * Obtener estadísticas de tickets por estado
     * 
     * @return Mapa con estadísticas de tickets
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getTicketStatistics() {
        Map<String, Long> statistics = ticketAdminService.getTicketStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Marcar tickets específicos como vencidos manualmente
     * 
     * @param ticketIds Lista de IDs de tickets a marcar como vencidos
     * @return Resultado de la operación
     */
    @PostMapping("/mark-specific-expired")
    public ResponseEntity<Map<String, Object>> markSpecificTicketsExpired(@RequestBody List<Long> ticketIds) {
        Map<String, Object> result = ticketAdminService.markSpecificTicketsExpired(ticketIds);
        return ResponseEntity.ok(result);
    }
} 