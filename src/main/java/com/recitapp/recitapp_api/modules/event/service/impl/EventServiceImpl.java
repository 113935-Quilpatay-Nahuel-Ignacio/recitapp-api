package com.recitapp.recitapp_api.modules.event.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistRepository;
import com.recitapp.recitapp_api.modules.event.dto.*;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.entity.EventArtist;
import com.recitapp.recitapp_api.modules.event.entity.EventStatus;
import com.recitapp.recitapp_api.modules.event.repository.EventArtistRepository;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.repository.EventStatisticsRepository;
import com.recitapp.recitapp_api.modules.event.repository.EventStatusRepository;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import com.recitapp.recitapp_api.modules.venue.repository.VenueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventStatusRepository eventStatusRepository;
    private final ArtistRepository artistRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final EventArtistRepository eventArtistRepository;
    private final TicketRepository ticketRepository;
    private final EventStatisticsRepository eventStatisticsRepository;

    // Actualiza el método createEvent con mejor manejo de excepciones
    @Override
    @Transactional
    public EventDTO createEvent(EventDTO eventDTO, Long registrarId) {
        // Validar que el venue existe
        Venue venue = venueRepository.findById(eventDTO.getVenueId())
                .orElseThrow(() -> EntityNotFoundException.create("Recinto", eventDTO.getVenueId()));

        // Validar que el artista principal existe (si se proporcionó)
        Artist mainArtist = null;
        if (eventDTO.getMainArtistId() != null) {
            mainArtist = artistRepository.findById(eventDTO.getMainArtistId())
                    .orElseThrow(() -> EntityNotFoundException.create("Artista", eventDTO.getMainArtistId()));
        }

        // Validar que el usuario registrador existe
        User registrar = userRepository.findById(registrarId)
                .orElseThrow(() -> EntityNotFoundException.create("Usuario", registrarId));

        // Validar que el usuario tiene permiso para registrar eventos
        if (!registrar.getRole().getName().equals("REGISTRADOR_EVENTO") &&
                !registrar.getRole().getName().equals("ADMIN")) {
            throw RecitappException.operationNotAllowed("Evento",
                    "El usuario no tiene permisos para registrar eventos");
        }

        // Validar disponibilidad del venue en las fechas proporcionadas
        if (eventRepository.existsOverlappingEvent(venue.getId(), eventDTO.getStartDateTime(), eventDTO.getEndDateTime())) {
            throw RecitappException.operationNotAllowed("Evento",
                    "El recinto no está disponible para las fechas seleccionadas");
        }

        // Validar que la fecha de fin es posterior a la fecha de inicio
        if (eventDTO.getEndDateTime().isBefore(eventDTO.getStartDateTime())) {
            throw new RecitappException("La fecha de finalización debe ser posterior a la fecha de inicio");
        }

        // Por defecto, crear el evento con estado "PROXIMO"
        EventStatus status = eventStatusRepository.findByName("PROXIMO")
                .orElseThrow(() -> EntityNotFoundException.createByName("Estado de evento", "PROXIMO"));

        // Crear la entidad Event
        Event event = new Event();
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setStartDateTime(eventDTO.getStartDateTime());
        event.setEndDateTime(eventDTO.getEndDateTime());
        event.setVenue(venue);
        event.setMainArtist(mainArtist);
        event.setStatus(status);
        event.setRegistrar(registrar);
        event.setFlyerImage(eventDTO.getFlyerImage());
        event.setVerified(false); // Por defecto, el evento no está verificado
        event.setSalesStartDate(eventDTO.getSalesStartDate());
        event.setSalesEndDate(eventDTO.getSalesEndDate());

        // Guardar el evento
        Event savedEvent;
        try {
            savedEvent = eventRepository.save(event);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage().contains("Duplicate entry") && ex.getMessage().contains("name")) {
                throw RecitappException.alreadyExists("Evento", "nombre: " + eventDTO.getName());
            }
            throw ex;
        }

        // Si se proporcionaron artistas adicionales, guardarlos
        if (eventDTO.getArtistIds() != null && !eventDTO.getArtistIds().isEmpty()) {
            LocalDateTime start = eventDTO.getStartDateTime();
            for (Long artistId : eventDTO.getArtistIds()) {
                Artist artist = artistRepository.findById(artistId)
                        .orElseThrow(() -> EntityNotFoundException.create("Artista", artistId));

                EventArtist eventArtist = new EventArtist();
                eventArtist.getId().setEventId(savedEvent.getId());
                eventArtist.getId().setArtistId(artist.getId());
                eventArtist.setEvent(savedEvent);
                eventArtist.setArtist(artist);
                eventArtist.setStartDateTime(start);
                eventArtist.setEndDateTime(start.plusMinutes(45)); // Default 45 min set per artist
                eventArtist.setAppearanceOrder(eventDTO.getArtistIds().indexOf(artistId) + 1);

                try {
                    eventArtistRepository.save(eventArtist);
                } catch (Exception ex) {
                    throw new RecitappException("Error al asociar el artista con ID " + artistId +
                            " al evento. Detalles: " + ex.getMessage(), ex);
                }

                // Increment start time for next artist
                start = start.plusMinutes(60); // 45 min performance + 15 min break
            }
        }

        return mapToDTO(savedEvent);
    }

    @Override
    @Transactional
    public EventDTO updateEvent(Long eventId, EventDTO eventDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Actualizar los campos del evento
        if (eventDTO.getName() != null) {
            event.setName(eventDTO.getName());
        }

        if (eventDTO.getDescription() != null) {
            event.setDescription(eventDTO.getDescription());
        }

        if (eventDTO.getStartDateTime() != null) {
            event.setStartDateTime(eventDTO.getStartDateTime());
        }

        if (eventDTO.getEndDateTime() != null) {
            event.setEndDateTime(eventDTO.getEndDateTime());
        }

        if (eventDTO.getFlyerImage() != null) {
            event.setFlyerImage(eventDTO.getFlyerImage());
        }

        if (eventDTO.getSalesStartDate() != null) {
            event.setSalesStartDate(eventDTO.getSalesStartDate());
        }

        if (eventDTO.getSalesEndDate() != null) {
            event.setSalesEndDate(eventDTO.getSalesEndDate());
        }

        // Actualizar el venue si se proporcionó
        if (eventDTO.getVenueId() != null) {
            Venue venue = venueRepository.findById(eventDTO.getVenueId())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with ID: " + eventDTO.getVenueId()));
            event.setVenue(venue);
        }

        // Actualizar el artista principal si se proporcionó
        if (eventDTO.getMainArtistId() != null) {
            Artist mainArtist = artistRepository.findById(eventDTO.getMainArtistId())
                    .orElseThrow(() -> new EntityNotFoundException("Artist not found with ID: " + eventDTO.getMainArtistId()));
            event.setMainArtist(mainArtist);
        }

        // Actualizar el estado si se proporcionó
        if (eventDTO.getStatusName() != null) {
            EventStatus status = eventStatusRepository.findByName(eventDTO.getStatusName())
                    .orElseThrow(() -> new EntityNotFoundException("Event status not found: " + eventDTO.getStatusName()));
            event.setStatus(status);
        }

        // Guardar los cambios
        Event updatedEvent = eventRepository.save(event);

        // Actualizar artistas asociados si se proporcionaron
        if (eventDTO.getArtistIds() != null) {
            // Eliminar las asociaciones actuales
            eventArtistRepository.deleteByEventId(eventId);

            // Crear nuevas asociaciones
            LocalDateTime start = eventDTO.getStartDateTime() != null ?
                    eventDTO.getStartDateTime() : event.getStartDateTime();

            for (Long artistId : eventDTO.getArtistIds()) {
                Artist artist = artistRepository.findById(artistId)
                        .orElseThrow(() -> new EntityNotFoundException("Artist not found with ID: " + artistId));

                EventArtist eventArtist = new EventArtist();
                eventArtist.getId().setEventId(updatedEvent.getId());
                eventArtist.getId().setArtistId(artist.getId());
                eventArtist.setEvent(updatedEvent);
                eventArtist.setArtist(artist);
                eventArtist.setStartDateTime(start);
                eventArtist.setEndDateTime(start.plusMinutes(45));
                eventArtist.setAppearanceOrder(eventDTO.getArtistIds().indexOf(artistId) + 1);

                eventArtistRepository.save(eventArtist);

                // Increment start time for next artist
                start = start.plusMinutes(60);
            }
        }

        return mapToDTO(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> EntityNotFoundException.create("Evento", eventId));

        // No se deben eliminar eventos con entradas vendidas
        if (ticketRepository.countSoldTicketsByEventId(eventId) > 0) {
            throw RecitappException.operationNotAllowed("Evento",
                    "No se puede eliminar un evento con entradas vendidas. Cambie el estado a CANCELADO en su lugar.");
        }

        // No se deben eliminar eventos que no estén cancelados, a menos que no estén verificados
        if (event.getVerified() && !event.getStatus().getName().equals("CANCELADO")) {
            throw RecitappException.operationNotAllowed("Evento",
                    "Solo se pueden eliminar eventos que estén cancelados o que no hayan sido verificados.");
        }

        try {
            // Eliminar las asociaciones con artistas
            eventArtistRepository.deleteByEventId(eventId);

            // Eliminar el evento
            eventRepository.delete(event);
        } catch (Exception ex) {
            throw new RecitappException("Error al eliminar el evento. Detalles: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailDTO getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Obtener los artistas asociados al evento
        List<EventArtist> eventArtists = eventArtistRepository.findByEventId(eventId);
        List<Long> artistIds = eventArtists.stream()
                .map(ea -> ea.getArtist().getId())
                .collect(Collectors.toList());

        // Mapear a DTO
        EventDetailDTO detailDTO = new EventDetailDTO();
        detailDTO.setId(event.getId());
        detailDTO.setName(event.getName());
        detailDTO.setDescription(event.getDescription());
        detailDTO.setStartDateTime(event.getStartDateTime());
        detailDTO.setEndDateTime(event.getEndDateTime());
        detailDTO.setVenueId(event.getVenue().getId());
        detailDTO.setVenueName(event.getVenue().getName());
        if (event.getMainArtist() != null) {
            detailDTO.setMainArtistId(event.getMainArtist().getId());
            detailDTO.setMainArtistName(event.getMainArtist().getName());
        }
        detailDTO.setStatusName(event.getStatus().getName());
        detailDTO.setFlyerImage(event.getFlyerImage());
        detailDTO.setVerified(event.getVerified());
        detailDTO.setSalesStartDate(event.getSalesStartDate());
        detailDTO.setSalesEndDate(event.getSalesEndDate());
        detailDTO.setRegistrationDate(event.getRegistrationDate());
        detailDTO.setUpdatedAt(event.getUpdatedAt());
        detailDTO.setArtistIds(artistIds);

        // Estadísticas básicas
        detailDTO.setTotalTickets(ticketRepository.countByEventId(eventId));
        detailDTO.setSoldTickets(ticketRepository.countSoldTicketsByEventId(eventId));

        return detailDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getAllEvents(Boolean upcomingOnly) {
        List<Event> events;

        if (upcomingOnly != null && upcomingOnly) {
            events = eventRepository.findUpcomingEvents(LocalDateTime.now());
        } else {
            events = eventRepository.findAll();
        }

        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> searchEvents(EventFilterDTO filterDTO) {
        List<Event> events = eventRepository.findByFilters(
                filterDTO.getStartDate(),
                filterDTO.getEndDate(),
                filterDTO.getVenueId(),
                filterDTO.getArtistId(),
                filterDTO.getStatusName(),
                filterDTO.getVerified(),
                filterDTO.getModeratorId(),
                filterDTO.getRegistrarId()
        );

        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventDTO verifyEvent(Long eventId, Long moderatorId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> EntityNotFoundException.create("Evento", eventId));

        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> EntityNotFoundException.create("Usuario", moderatorId));

        // Verificar que el usuario es un moderador
        if (!moderator.getRole().getName().equals("MODERADOR") &&
                !moderator.getRole().getName().equals("ADMIN")) {
            throw RecitappException.operationNotAllowed("Evento",
                    "El usuario no tiene permisos de moderador para verificar eventos");
        }

        // Verificar que el evento no esté ya verificado
        if (event.getVerified()) {
            throw RecitappException.operationNotAllowed("Evento",
                    "El evento ya ha sido verificado");
        }

        // Actualizar el estado del evento
        event.setVerified(true);
        event.setModerator(moderator);

        // Si el evento estaba en estado "PROXIMO" y tiene fechas de venta de entradas,
        // actualizar a "EN_VENTA" si la fecha de inicio de ventas ya pasó
        if (event.getStatus().getName().equals("PROXIMO") &&
                event.getSalesStartDate() != null &&
                event.getSalesStartDate().isBefore(LocalDateTime.now())) {

            EventStatus enVentaStatus = eventStatusRepository.findByName("EN_VENTA")
                    .orElseThrow(() -> EntityNotFoundException.createByName("Estado de evento", "EN_VENTA"));

            event.setStatus(enVentaStatus);
        }

        Event verifiedEvent = eventRepository.save(event);

        return mapToDTO(verifiedEvent);
    }

    @Override
    @Transactional
    public EventDTO verifyEventWithDetails(Long eventId, EventVerificationRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        User moderator = userRepository.findById(request.getModeratorId())
                .orElseThrow(() -> new EntityNotFoundException("Moderator not found with ID: " + request.getModeratorId()));

        // Verificar que el usuario es un moderador
        if (!moderator.getRole().getName().equals("MODERADOR") && !moderator.getRole().getName().equals("ADMIN")) {
            throw new RecitappException("User does not have moderator permissions");
        }

        // Actualizar el estado del evento
        event.setVerified(true);
        event.setModerator(moderator);

        // Si se solicitó actualizar el estado
        if (request.getUpdateStatus() != null && request.getUpdateStatus() && request.getNewStatus() != null) {
            EventStatus newStatus = eventStatusRepository.findByName(request.getNewStatus())
                    .orElseThrow(() -> new EntityNotFoundException("Event status not found: " + request.getNewStatus()));

            // Validar la transición de estado
            validateStatusTransition(event.getStatus().getName(), request.getNewStatus());

            event.setStatus(newStatus);
        } else if (event.getStatus().getName().equals("PROXIMO") &&
                event.getSalesStartDate() != null &&
                event.getSalesStartDate().isBefore(LocalDateTime.now())) {
            // actualizar a "EN_VENTA" si la fecha de inicio de ventas ya pasó
            EventStatus enVentaStatus = eventStatusRepository.findByName("EN_VENTA")
                    .orElseThrow(() -> new EntityNotFoundException("Event status 'EN_VENTA' not found"));

            event.setStatus(enVentaStatus);
        }

        // Aquí se podría implementar la lógica para guardar los comentarios de verificación
        // en una tabla adicional o en un campo específico del evento

        Event verifiedEvent = eventRepository.save(event);

        return mapToDTO(verifiedEvent);
    }

    @Override
    @Transactional
    public EventDTO updateEventStatus(Long eventId, String statusName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        EventStatus newStatus = eventStatusRepository.findByName(statusName)
                .orElseThrow(() -> new EntityNotFoundException("Event status not found: " + statusName));

        // Validar la transición de estado
        validateStatusTransition(event.getStatus().getName(), statusName);

        // Actualizar el estado
        event.setStatus(newStatus);
        Event updatedEvent = eventRepository.save(event);

        return mapToDTO(updatedEvent);
    }

    @Override
    @Transactional
    public void cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> EntityNotFoundException.create("Evento", eventId));

        // Verificar que el evento no esté ya cancelado
        if (event.getStatus().getName().equals("CANCELADO")) {
            throw RecitappException.operationNotAllowed("Evento",
                    "El evento ya está cancelado");
        }

        // Verificar que el evento no esté finalizado
        if (event.getStatus().getName().equals("FINALIZADO")) {
            throw RecitappException.operationNotAllowed("Evento",
                    "No se puede cancelar un evento que ya ha finalizado");
        }

        EventStatus canceledStatus = eventStatusRepository.findByName("CANCELADO")
                .orElseThrow(() -> EntityNotFoundException.createByName("Estado de evento", "CANCELADO"));

        event.setStatus(canceledStatus);
        eventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsByVenue(Long venueId) {
        venueRepository.findById(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with ID: " + venueId));

        List<Event> events = eventRepository.findByVenueId(venueId);

        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsByArtist(Long artistId) {
        artistRepository.findById(artistId)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with ID: " + artistId));

        List<Event> events = eventRepository.findByArtistId(artistId);

        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }

        if (endDate == null) {
            endDate = startDate.plusMonths(3); // Default: próximos 3 meses
        }

        List<Event> events = eventRepository.findByDateRange(startDate, endDate);

        return events.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventStatisticsDTO getEventStatistics(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + eventId));

        // Obtener estadísticas de tickets
        Long totalTickets = ticketRepository.countByEventId(eventId);
        Long soldTickets = ticketRepository.countSoldTicketsByEventId(eventId);
        Double occupancyRate = totalTickets > 0 ? (soldTickets.doubleValue() / totalTickets.doubleValue()) * 100 : 0;

        return EventStatisticsDTO.builder()
                .eventId(event.getId())
                .eventName(event.getName())
                .totalTickets(totalTickets)
                .soldTickets(soldTickets)
                .occupancyRate(occupancyRate)
                .statusName(event.getStatus().getName())
                .build();
    }

    @Override
    @Transactional
    public void cleanupCanceledEvents(LocalDateTime cutoffDate) {
        // Encontrar eventos cancelados antes de la fecha de corte
        List<Event> canceledEvents = eventRepository.findCanceledEventsBefore(cutoffDate);

        for (Event event : canceledEvents) {
            // Verificar que no haya entradas vendidas
            if (ticketRepository.countSoldTicketsByEventId(event.getId()) == 0) {
                // Eliminar las asociaciones con artistas
                eventArtistRepository.deleteByEventId(event.getId());

                // Eliminar el evento
                eventRepository.delete(event);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }

    // Métodos auxiliares
    private EventDTO mapToDTO(Event event) {
        EventDTO dto = EventDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .mainArtistId(event.getMainArtist() != null ? event.getMainArtist().getId() : null)
                .mainArtistName(event.getMainArtist() != null ? event.getMainArtist().getName() : null)
                .statusName(event.getStatus().getName())
                .flyerImage(event.getFlyerImage())
                .salesStartDate(event.getSalesStartDate())
                .salesEndDate(event.getSalesEndDate())
                .verified(event.getVerified())
                .build();

        // Obtener IDs de artistas adicionales si hay alguno
        List<Long> artistIds = eventArtistRepository.findByEventId(event.getId()).stream()
                .map(ea -> ea.getArtist().getId())
                .collect(Collectors.toList());

        dto.setArtistIds(artistIds);

        // Agregar IDs de moderador y registrador si están disponibles
        if (event.getModerator() != null) {
            dto.setModeratorId(event.getModerator().getId());
        }

        if (event.getRegistrar() != null) {
            dto.setRegistrarId(event.getRegistrar().getId());
        }

        return dto;
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Reglas de transición de estado
        if (currentStatus.equals("CANCELADO") && !newStatus.equals("CANCELADO")) {
            throw new RecitappException("Cannot change status of a canceled event");
        }

        if (currentStatus.equals("FINALIZADO") && !newStatus.equals("FINALIZADO")) {
            throw new RecitappException("Cannot change status of a finished event");
        }

        if (currentStatus.equals("PROXIMO") && newStatus.equals("FINALIZADO")) {
            throw new RecitappException("Cannot directly change from PROXIMO to FINALIZADO");
        }

        if (currentStatus.equals("AGOTADO") && newStatus.equals("EN_VENTA")) {
            throw new RecitappException("Cannot change from AGOTADO to EN_VENTA");
        }
    }
}