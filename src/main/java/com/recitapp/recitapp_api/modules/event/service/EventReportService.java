package com.recitapp.recitapp_api.modules.event.service;

import com.recitapp.recitapp_api.modules.event.dto.EventFilterDTO;
import com.recitapp.recitapp_api.modules.event.dto.EventStatisticsDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la generación de informes y estadísticas de eventos
 */
public interface EventReportService {

    /**
     * Genera un informe de asistencia a eventos
     *
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     * @param venueId ID opcional del recinto
     * @param statusName Nombre opcional del estado
     * @return Mapa con el informe de asistencia
     */
    Map<String, Object> generateAttendanceReport(LocalDateTime startDate, LocalDateTime endDate,
                                                 Long venueId, String statusName);

    /**
     * Genera un informe de ventas de entradas
     *
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     * @param venueId ID opcional del recinto
     * @param artistId ID opcional del artista
     * @return Mapa con el informe de ventas
     */
    Map<String, Object> generateTicketSalesReport(LocalDateTime startDate, LocalDateTime endDate,
                                                  Long venueId, Long artistId);

    /**
     * Obtiene estadísticas para múltiples eventos según filtros
     *
     * @param filterDTO Criterios de filtrado
     * @return Lista de DTOs con estadísticas de eventos
     */
    List<EventStatisticsDTO> getEventsStatistics(EventFilterDTO filterDTO);

    /**
     * Obtiene los eventos más populares según asistencia/entradas vendidas
     *
     * @param limit Cantidad máxima de eventos a devolver
     * @param startDate Fecha de inicio del período
     * @param endDate Fecha de fin del período
     * @return Lista de DTOs con estadísticas de eventos populares
     */
    List<EventStatisticsDTO> getPopularEvents(int limit, LocalDateTime startDate, LocalDateTime endDate);
}