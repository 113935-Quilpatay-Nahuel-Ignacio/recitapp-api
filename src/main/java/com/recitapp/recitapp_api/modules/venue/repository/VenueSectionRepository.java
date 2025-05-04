package com.recitapp.recitapp_api.modules.venue.repository;

import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueSectionRepository extends JpaRepository<VenueSection, Long> {

    List<VenueSection> findByVenueId(Long venueId);

    @Query("SELECT SUM(vs.capacity) FROM VenueSection vs WHERE vs.venue.id = :venueId AND vs.active = true")
    Integer calculateTotalCapacity(@Param("venueId") Long venueId);

    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN true ELSE false END FROM TicketPrice tp WHERE tp.section.id = :sectionId")
    boolean hasTicketPrices(@Param("sectionId") Long sectionId);
}