package com.recitapp.recitapp_api.modules.venue.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.venue.dto.*;
import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import com.recitapp.recitapp_api.modules.venue.repository.VenueRepository;
import com.recitapp.recitapp_api.modules.venue.repository.VenueSectionRepository;
import com.recitapp.recitapp_api.modules.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final VenueSectionRepository venueSectionRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public VenueDTO createVenue(VenueDTO venueDTO) {
        if (venueRepository.existsByName(venueDTO.getName())) {
            throw new RecitappException("Ya existe un recinto con el nombre: " + venueDTO.getName());
        }

        Venue venue = mapToEntity(venueDTO);
        venue.setActive(true);
        
        // Lógica de validación de capacidades
        if (venueDTO.getSections() != null && !venueDTO.getSections().isEmpty()) {
            int sectionsCapacitySum = venueDTO.getSections().stream()
                    .mapToInt(VenueSectionDTO::getCapacity)
                    .sum();
            
            if (venueDTO.getTotalCapacity() != null) {
                // Si se especificó capacidad total, debe coincidir con la suma de secciones
                if (!venueDTO.getTotalCapacity().equals(sectionsCapacitySum)) {
                    throw new RecitappException(
                        String.format("La capacidad total especificada (%d) no coincide con la suma de las capacidades de las secciones (%d). " +
                                     "Las capacidades deben ser iguales.", 
                                     venueDTO.getTotalCapacity(), sectionsCapacitySum)
                    );
                }
            } else {
                // Si no se especificó capacidad total, se calcula automáticamente
                venue.setTotalCapacity(sectionsCapacitySum);
            }
        } else if (venueDTO.getTotalCapacity() == null) {
            // Si no hay secciones ni capacidad total especificada, se establece en 0
            venue.setTotalCapacity(0);
        }
        
        Venue savedVenue = venueRepository.save(venue);

        // Si se proporcionaron secciones, crearlas después de guardar el venue
        if (venueDTO.getSections() != null && !venueDTO.getSections().isEmpty()) {
            for (VenueSectionDTO sectionDTO : venueDTO.getSections()) {
                createVenueSectionInternal(savedVenue.getId(), sectionDTO);
            }
        }

        return mapToDTO(savedVenue);
    }

    @Override
    @Transactional
    public VenueDTO updateVenue(Long id, VenueUpdateDTO venueDTO) {
        Venue venue = findVenueById(id);

        if (!venue.getName().equals(venueDTO.getName()) &&
                venueRepository.existsByName(venueDTO.getName())) {
            throw new RecitappException("Ya existe otro recinto con el nombre: " + venueDTO.getName());
        }

        // Validar capacidad total si se está actualizando
        if (venueDTO.getTotalCapacity() != null) {
            Integer currentSectionsCapacity = venueSectionRepository.calculateTotalCapacity(id);
            if (currentSectionsCapacity != null && currentSectionsCapacity > 0) {
                if (!venueDTO.getTotalCapacity().equals(currentSectionsCapacity)) {
                    throw new RecitappException(
                        String.format("La nueva capacidad total (%d) no coincide con la suma de las capacidades de las secciones existentes (%d). " +
                                     "Para cambiar la capacidad total, primero ajuste las capacidades de las secciones.", 
                                     venueDTO.getTotalCapacity(), currentSectionsCapacity)
                    );
                }
            }
        }

        if (venueDTO.getName() != null) {
            venue.setName(venueDTO.getName());
        }
        if (venueDTO.getAddress() != null) {
            venue.setAddress(venueDTO.getAddress());
        }
        if (venueDTO.getGoogleMapsUrl() != null) {
            venue.setGoogleMapsUrl(venueDTO.getGoogleMapsUrl());
        }
        if (venueDTO.getTotalCapacity() != null) {
            venue.setTotalCapacity(venueDTO.getTotalCapacity());
        }
        if (venueDTO.getDescription() != null) {
            venue.setDescription(venueDTO.getDescription());
        }
        if (venueDTO.getInstagramUrl() != null) {
            venue.setInstagramUrl(venueDTO.getInstagramUrl());
        }
        if (venueDTO.getWebUrl() != null) {
            venue.setWebUrl(venueDTO.getWebUrl());
        }
        if (venueDTO.getImage() != null) {
            venue.setImage(venueDTO.getImage());
        }
        if (venueDTO.getLatitude() != null) {
            venue.setLatitude(venueDTO.getLatitude());
        }
        if (venueDTO.getLongitude() != null) {
            venue.setLongitude(venueDTO.getLongitude());
        }

        Venue updatedVenue = venueRepository.save(venue);
        return mapToDTO(updatedVenue);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = findVenueById(id);

        // Verificar si tiene eventos asociados antes de eliminar
        List<Event> events = eventRepository.findByVenueId(id);
        if (!events.isEmpty()) {
            throw new RecitappException("No se puede eliminar el recinto porque tiene eventos asociados. " +
                    "Considere desactivarlo en su lugar.");
        }

        venueRepository.deleteById(id);
    }

    @Override
    @Transactional
    public VenueDTO deactivateVenue(Long id) {
        Venue venue = findVenueById(id);
        venue.setActive(false);
        Venue updatedVenue = venueRepository.save(venue);
        return mapToDTO(updatedVenue);
    }

    @Override
    @Transactional
    public VenueDTO activateVenue(Long id) {
        Venue venue = findVenueById(id);
        venue.setActive(true);
        Venue updatedVenue = venueRepository.save(venue);
        return mapToDTO(updatedVenue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDTO> getAvailableVenues(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Venue> availableVenues = venueRepository.findAvailableVenues(startDateTime, endDateTime);
        return availableVenues.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VenueAvailabilityDTO checkVenueAvailability(Long venueId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Venue venue = findVenueById(venueId);

        // Buscar eventos que se superpongan con el período solicitado
        List<Event> conflictingEvents = eventRepository.findConflictingEvents(venueId, startDateTime, endDateTime);

        boolean isAvailable = conflictingEvents.isEmpty();

        List<EventConflictDTO> conflicts = conflictingEvents.stream()
                .map(event -> EventConflictDTO.builder()
                        .eventId(event.getId())
                        .eventName(event.getName())
                        .startDateTime(event.getStartDateTime())
                        .endDateTime(event.getEndDateTime())
                        .build())
                .collect(Collectors.toList());

        return VenueAvailabilityDTO.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .isAvailable(isAvailable)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .conflictingEvents(conflicts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getVenueEvents(Long venueId, Boolean includePastEvents) {
        findVenueById(venueId); // Verificar que existe

        List<Event> events;
        if (includePastEvents != null && includePastEvents) {
            events = eventRepository.findByVenueId(venueId);
        } else {
            events = eventRepository.findUpcomingByVenueId(venueId, LocalDateTime.now());
        }

        return events.stream()
                .map(this::mapEventToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VenueStatisticsDTO getVenueStatistics(Long venueId) {
        Venue venue = findVenueById(venueId);

        LocalDateTime now = LocalDateTime.now();
        Integer totalEvents = eventRepository.countByVenueId(venueId);
        Integer upcomingEvents = eventRepository.countUpcomingByVenueId(venueId, now);
        Integer pastEvents = eventRepository.countPastByVenueId(venueId, now);

        // Calcular tasa de ocupación (eventos / capacidad total)
        Double occupancyRate = calculateOccupancyRate(venueId);

        return VenueStatisticsDTO.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .pastEvents(pastEvents)
                .occupancyRate(occupancyRate)
                .lastUpdateDate(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueStatisticsDTO> getVenuesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> venuesWithCount = venueRepository.findVenuesWithEventCount(startDate, endDate);

        return venuesWithCount.stream()
                .map(result -> {
                    Venue venue = (Venue) result[0];
                    Long eventCount = (Long) result[1];

                    return VenueStatisticsDTO.builder()
                            .venueId(venue.getId())
                            .venueName(venue.getName())
                            .totalEvents(eventCount.intValue())
                            .lastUpdateDate(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VenueSectionDTO createVenueSection(Long venueId, VenueSectionDTO sectionDTO) {
        Venue venue = findVenueById(venueId);
        
        // Validar que la nueva sección no haga que se exceda la capacidad total especificada
        if (venue.getTotalCapacity() != null) {
            Integer currentSectionsCapacity = venueSectionRepository.calculateTotalCapacity(venueId);
            if (currentSectionsCapacity == null) currentSectionsCapacity = 0;
            
            int newTotalCapacity = currentSectionsCapacity + sectionDTO.getCapacity();
            if (newTotalCapacity > venue.getTotalCapacity()) {
                throw new RecitappException(
                    String.format("No se puede crear la sección. La capacidad resultante (%d) excedería la capacidad total del recinto (%d). " +
                                 "Capacidad disponible: %d", 
                                 newTotalCapacity, venue.getTotalCapacity(), venue.getTotalCapacity() - currentSectionsCapacity)
                );
            }
        }

        VenueSection section = new VenueSection();
        section.setVenue(venue);
        section.setName(sectionDTO.getName());
        section.setCapacity(sectionDTO.getCapacity());
        section.setDescription(sectionDTO.getDescription());
        section.setActive(true);

        VenueSection savedSection = venueSectionRepository.save(section);

        // Actualizar la capacidad total del recinto si no estaba especificada
        if (venue.getTotalCapacity() == null) {
            updateVenueTotalCapacity(venueId);
        }

        return mapSectionToDTO(savedSection);
    }
    
    /**
     * Método interno para crear secciones sin validaciones de capacidad
     * Se usa durante la creación inicial del venue cuando las validaciones ya se hicieron
     */
    private VenueSectionDTO createVenueSectionInternal(Long venueId, VenueSectionDTO sectionDTO) {
        Venue venue = findVenueById(venueId);

        VenueSection section = new VenueSection();
        section.setVenue(venue);
        section.setName(sectionDTO.getName());
        section.setCapacity(sectionDTO.getCapacity());
        section.setDescription(sectionDTO.getDescription());
        section.setActive(true);

        VenueSection savedSection = venueSectionRepository.save(section);
        return mapSectionToDTO(savedSection);
    }

    @Override
    @Transactional
    public VenueSectionDTO updateVenueSection(Long venueId, Long sectionId, VenueSectionDTO sectionDTO) {
        Venue venue = findVenueById(venueId);

        VenueSection section = venueSectionRepository.findById(sectionId)
                .orElseThrow(() -> new RecitappException("Sección no encontrada con ID: " + sectionId));

        if (!section.getVenue().getId().equals(venueId)) {
            throw new RecitappException("La sección no pertenece al recinto especificado");
        }

        // Validar capacidad si se está actualizando y el venue tiene capacidad total especificada
        if (sectionDTO.getCapacity() != null && venue.getTotalCapacity() != null) {
            Integer currentSectionsCapacity = venueSectionRepository.calculateTotalCapacity(venueId);
            if (currentSectionsCapacity == null) currentSectionsCapacity = 0;
            
            // Calcular nueva capacidad total considerando el cambio en esta sección
            int capacityDifference = sectionDTO.getCapacity() - section.getCapacity();
            int newTotalCapacity = currentSectionsCapacity + capacityDifference;
            
            if (newTotalCapacity > venue.getTotalCapacity()) {
                throw new RecitappException(
                    String.format("No se puede actualizar la sección. La capacidad resultante (%d) excedería la capacidad total del recinto (%d). " +
                                 "Capacidad máxima permitida para esta sección: %d", 
                                 newTotalCapacity, venue.getTotalCapacity(), 
                                 section.getCapacity() + (venue.getTotalCapacity() - currentSectionsCapacity))
                );
            }
        }

        if (sectionDTO.getName() != null) {
            section.setName(sectionDTO.getName());
        }
        if (sectionDTO.getCapacity() != null) {
            section.setCapacity(sectionDTO.getCapacity());
        }
        if (sectionDTO.getDescription() != null) {
            section.setDescription(sectionDTO.getDescription());
        }
        if (sectionDTO.getActive() != null) {
            section.setActive(sectionDTO.getActive());
        }

        VenueSection updatedSection = venueSectionRepository.save(section);

        // Solo actualizar capacidad total si el venue no tiene capacidad especificada
        if (venue.getTotalCapacity() == null) {
            updateVenueTotalCapacity(venueId);
        }

        return mapSectionToDTO(updatedSection);
    }

    @Override
    @Transactional
    public void deleteVenueSection(Long venueId, Long sectionId) {
        Venue venue = findVenueById(venueId); // Verificar que el recinto existe

        VenueSection section = venueSectionRepository.findById(sectionId)
                .orElseThrow(() -> new RecitappException("Sección no encontrada con ID: " + sectionId));

        if (!section.getVenue().getId().equals(venueId)) {
            throw new RecitappException("La sección no pertenece al recinto especificado");
        }

        // Verificar si hay eventos con esta sección
        if (eventHasSection(sectionId)) {
            throw new RecitappException("No se puede eliminar la sección porque está asociada a eventos");
        }

        venueSectionRepository.deleteById(sectionId);

        // Solo actualizar la capacidad total si el venue no tiene capacidad especificada
        if (venue.getTotalCapacity() == null) {
            updateVenueTotalCapacity(venueId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueSectionDTO> getVenueSections(Long venueId) {
        findVenueById(venueId); // Verificar que el recinto existe

        List<VenueSection> sections = venueSectionRepository.findByVenueId(venueId);

        return sections.stream()
                .map(this::mapSectionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VenueDTO updateVenueLocation(Long id, double latitude, double longitude) {
        Venue venue = findVenueById(id);

        venue.setLatitude(BigDecimal.valueOf(latitude));
        venue.setLongitude(BigDecimal.valueOf(longitude));

        Venue updatedVenue = venueRepository.save(venue);
        return mapToDTO(updatedVenue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDTO> findVenuesNearby(double latitude, double longitude, double radiusInKm) {
        // Convertir km a metros para la consulta
        double radiusInMeters = radiusInKm * 1000;

        List<Venue> nearbyVenues = venueRepository.findVenuesNearby(latitude, longitude, radiusInMeters);

        return nearbyVenues.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VenueDTO getVenueById(Long id) {
        Venue venue = findVenueById(id);
        return mapToDTO(venue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDTO> getAllVenues(Boolean activeOnly) {
        List<Venue> venues;

        if (activeOnly != null && activeOnly) {
            venues = venueRepository.findByActiveTrue();
        } else {
            venues = venueRepository.findAll();
        }

        return venues.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueDTO> searchVenuesByName(String name) {
        // Implementar búsqueda por nombre (podría requerir un método adicional en el repositorio)
        return venueRepository.findAll().stream()
                .filter(venue -> venue.getName().toLowerCase().contains(name.toLowerCase()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return venueRepository.existsById(id);
    }

    // Métodos auxiliares
    private Venue findVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Recinto no encontrado con ID: " + id));
    }

    private void updateVenueTotalCapacity(Long venueId) {
        Venue venue = findVenueById(venueId);
        Integer totalCapacity = venueSectionRepository.calculateTotalCapacity(venueId);
        venue.setTotalCapacity(totalCapacity);
        venueRepository.save(venue);
    }

    private boolean eventHasSection(Long sectionId) {
        return venueSectionRepository.hasTicketPrices(sectionId);
    }

    private Double calculateOccupancyRate(Long venueId) {
        // Aquí se podría implementar un cálculo más complejo, por ejemplo,
        // basado en la cantidad de tickets vendidos vs. la capacidad total
        return 0.0; // Por defecto
    }

    private Venue mapToEntity(VenueDTO dto) {
        Venue venue = new Venue();
        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setGoogleMapsUrl(dto.getGoogleMapsUrl());
        venue.setTotalCapacity(dto.getTotalCapacity());
        venue.setDescription(dto.getDescription());
        venue.setInstagramUrl(dto.getInstagramUrl());
        venue.setWebUrl(dto.getWebUrl());
        venue.setImage(dto.getImage());
        venue.setActive(dto.getActive() != null ? dto.getActive() : true);
        venue.setLatitude(dto.getLatitude());
        venue.setLongitude(dto.getLongitude());
        return venue;
    }

    private VenueDTO mapToDTO(Venue entity) {
        List<VenueSectionDTO> sections = null;
        if (entity.getSections() != null) {
            sections = entity.getSections().stream()
                    .map(this::mapSectionToDTO)
                    .collect(Collectors.toList());
        }
        
        return VenueDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .googleMapsUrl(entity.getGoogleMapsUrl())
                .totalCapacity(entity.getTotalCapacity())
                .description(entity.getDescription())
                .instagramUrl(entity.getInstagramUrl())
                .webUrl(entity.getWebUrl())
                .image(entity.getImage())
                .active(entity.getActive())
                .registrationDate(entity.getRegistrationDate())
                .updatedAt(entity.getUpdatedAt())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .sections(sections)
                .build();
    }

    private VenueSectionDTO mapSectionToDTO(VenueSection section) {
        return VenueSectionDTO.builder()
                .id(section.getId())
                .name(section.getName())
                .capacity(section.getCapacity())
                .description(section.getDescription())
                .active(section.getActive())
                .venueId(section.getVenue().getId())
                .build();
    }

    private EventDTO mapEventToDTO(Event event) {
        return EventDTO.builder()
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
                .build();
    }
}