package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.AccessPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for access point entities
 */
@Repository
public interface AccessPointRepository extends JpaRepository<AccessPoint, Long> {

    /**
     * Finds all access points for a specific event
     *
     * @param eventId The ID of the event
     * @return A list of access points
     */
    List<AccessPoint> findByEventId(Long eventId);

    /**
     * Finds all active access points for a specific event
     *
     * @param eventId The ID of the event
     * @return A list of active access points
     */
    List<AccessPoint> findByEventIdAndActiveTrue(Long eventId);
}