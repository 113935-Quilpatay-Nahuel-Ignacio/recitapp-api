package com.recitapp.recitapp_api.modules.venue.service;

import com.recitapp.recitapp_api.modules.venue.dto.*;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface VenueService {
    VenueDTO createVenue(VenueDTO venueDTO);

    VenueDTO updateVenue(Long id, VenueUpdateDTO venueDTO);

    void deleteVenue(Long id);
    VenueDTO deactivateVenue(Long id);
    VenueDTO activateVenue(Long id);

    List<VenueDTO> getAvailableVenues(LocalDateTime startDateTime, LocalDateTime endDateTime);
    VenueAvailabilityDTO checkVenueAvailability(Long venueId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<EventDTO> getVenueEvents(Long venueId, Boolean includePastEvents);

    VenueStatisticsDTO getVenueStatistics(Long venueId);
    List<VenueStatisticsDTO> getVenuesStatistics(LocalDateTime startDate, LocalDateTime endDate);

    VenueSectionDTO createVenueSection(Long venueId, VenueSectionDTO sectionDTO);
    VenueSectionDTO updateVenueSection(Long venueId, Long sectionId, VenueSectionDTO sectionDTO);
    void deleteVenueSection(Long venueId, Long sectionId);
    List<VenueSectionDTO> getVenueSections(Long venueId);

    VenueDTO updateVenueLocation(Long id, double latitude, double longitude);
    List<VenueDTO> findVenuesNearby(double latitude, double longitude, double radiusInKm);

    VenueDTO getVenueById(Long id);
    List<VenueDTO> getAllVenues(Boolean activeOnly);
    List<VenueDTO> searchVenuesByName(String name);
    boolean existsById(Long id);
}