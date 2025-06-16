package com.recitapp.recitapp_api.modules.ticket.service;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;

import java.util.List;
import java.util.Map;

/**
 * Servicio para operaciones administrativas de tickets
 */
public interface TicketAdminService {

    /**
     * Marca tickets de eventos pasados como vencidos (cambiar estado de VENDIDA a VENCIDA)
     * 
     * @return Mapa con información del resultado (tickets procesados, errores, etc.)
     */
    Map<String, Object> markExpiredTickets();

    /**
     * Obtiene lista de tickets que pueden ser marcados como vencidos
     * 
     * @return Lista de tickets que serían marcados como vencidos
     */
    List<TicketDTO> getTicketsToExpire();

    /**
     * Obtiene estadísticas de tickets agrupadas por estado
     * 
     * @return Mapa con estadísticas (estado -> cantidad)
     */
    Map<String, Long> getTicketStatistics();

    /**
     * Marca tickets específicos como vencidos manualmente
     * 
     * @param ticketIds Lista de IDs de tickets a marcar como vencidos
     * @return Mapa con información del resultado
     */
    Map<String, Object> markSpecificTicketsExpired(List<Long> ticketIds);
} 