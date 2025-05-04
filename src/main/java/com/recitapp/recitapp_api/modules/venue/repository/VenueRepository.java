package com.recitapp.recitapp_api.modules.venue.repository;

import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    boolean existsById(Long id);
}