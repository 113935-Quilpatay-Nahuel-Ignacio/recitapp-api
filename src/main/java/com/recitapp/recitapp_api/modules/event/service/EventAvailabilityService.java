package com.recitapp.recitapp_api.modules.event.service;

import com.recitapp.recitapp_api.modules.venue.dto.SectionAvailabilityDTO;

import java.util.List;

public interface EventAvailabilityService {

    /**
     * Get availability information for all sections of an event
     *
     * @param eventId ID of the event
     * @return List of availability information for each section
     */
    List<SectionAvailabilityDTO> getEventSectionsAvailability(Long eventId);

    /**
     * Get availability information for a specific section of an event
     *
     * @param eventId ID of the event
     * @param sectionId ID of the section
     * @return Availability information for the specified section
     */
    SectionAvailabilityDTO getSectionAvailability(Long eventId, Long sectionId);
}