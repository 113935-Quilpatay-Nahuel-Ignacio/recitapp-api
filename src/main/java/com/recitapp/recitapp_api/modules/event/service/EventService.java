package com.recitapp.recitapp_api.modules.event.service;

import com.recitapp.recitapp_api.modules.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    /**
     * Crea un nuevo evento musical
     *
     * @param eventDTO Datos del evento a crear
     * @param registrarId ID del usuario que registra el evento
     * @return DTO con información del evento creado
     */
    EventDTO createEvent(EventDTO eventDTO, Long registrarId);

    /**
     * Actualiza la información de un evento existente
     *
     * @param eventId ID del evento a actualizar
     * @param eventDTO Datos actualizados del evento
     * @return DTO con información del evento actualizado
     */
    EventDTO updateEvent(Long eventId, EventDTO eventDTO);

    /**
     * Elimina un evento existente
     *
     * @param eventId ID del evento a eliminar
     */
    void deleteEvent(Long eventId);

    /**
     * Obtiene información detallada de un evento
     *
     * @param eventId ID del evento
     * @return DTO con información detallada del evento
     */
    EventDetailDTO getEventDetail(Long eventId);

    /**
     * Obtiene información completa de un evento para edición (incluye precios de tickets)
     *
     * @param eventId ID del evento
     * @return DTO con información completa del evento para edición
     */
    EventDTO getEventForEdit(Long eventId);

    /**
     * Obtiene todos los eventos, con opción de filtrar solo por próximos eventos
     *
     * @param upcomingOnly Si es true, solo se obtienen eventos futuros
     * @return Lista de DTOs con información de los eventos
     */
    List<EventDTO> getAllEvents(Boolean upcomingOnly);

    /**
     * Busca eventos según varios criterios de filtrado
     *
     * @param filterDTO Objeto con los criterios de filtrado
     * @return Lista de DTOs con información de los eventos
     */
    List<EventDTO> searchEvents(EventFilterDTO filterDTO);

    /**
     * Verifica la legitimidad de un evento propuesto
     *
     * @param eventId ID del evento a verificar
     * @param moderatorId ID del moderador que realiza la verificación
     * @return DTO con información del evento verificado
     */
    EventDTO verifyEvent(Long eventId, Long moderatorId);

    /**
     * Verifica la legitimidad de un evento propuesto con información adicional
     *
     * @param eventId ID del evento a verificar
     * @param request Datos para la verificación
     * @return DTO con información del evento verificado
     */
    EventDTO verifyEventWithDetails(Long eventId, EventVerificationRequest request);

    /**
     * Actualiza el estado de un evento
     *
     * @param eventId ID del evento
     * @param statusName Nombre del nuevo estado
     * @return DTO con información del evento actualizado
     */
    EventDTO updateEventStatus(Long eventId, String statusName);

    /**
     * Marca un evento como cancelado
     *
     * @param eventId ID del evento a cancelar
     */
    void cancelEvent(Long eventId);

    /**
     * Obtiene eventos por recinto (venue)
     *
     * @param venueId ID del recinto
     * @return Lista de DTOs con información de los eventos
     */
    List<EventDTO> getEventsByVenue(Long venueId);

    /**
     * Obtiene eventos por artista
     *
     * @param artistId ID del artista
     * @return Lista de DTOs con información de los eventos
     */
    List<EventDTO> getEventsByArtist(Long artistId);

    /**
     * Obtiene eventos en un rango de fechas
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de DTOs con información de los eventos
     */
    List<EventDTO> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Obtiene estadísticas de un evento
     *
     * @param eventId ID del evento
     * @return DTO con estadísticas del evento
     */
    EventStatisticsDTO getEventStatistics(Long eventId);

    /**
     * Elimina eventos cancelados antes de cierta fecha
     *
     * @param cutoffDate Fecha límite
     */
    void cleanupCanceledEvents(LocalDateTime cutoffDate);

    /**
     * Verifica si existe un evento con el ID proporcionado
     *
     * @param id ID del evento
     * @return true si existe, false en caso contrario
     */
    boolean existsById(Long id);

    boolean isEventSoldOut(Long eventId);

}