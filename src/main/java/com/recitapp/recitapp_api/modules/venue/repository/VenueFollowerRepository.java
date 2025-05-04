package com.recitapp.recitapp_api.modules.venue.repository;

import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import com.recitapp.recitapp_api.modules.venue.entity.VenueFollower;
import com.recitapp.recitapp_api.modules.venue.entity.VenueFollowerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueFollowerRepository extends JpaRepository<VenueFollower, VenueFollowerId> {

    @Query("SELECT vf FROM VenueFollower vf WHERE vf.user.id = :userId")
    List<VenueFollower> findAllByUserId(Long userId);

    @Query("SELECT CASE WHEN COUNT(vf) > 0 THEN true ELSE false END FROM VenueFollower vf WHERE vf.user.id = :userId AND vf.venue.id = :venueId")
    boolean existsByUserIdAndVenueId(Long userId, Long venueId);

    Optional<VenueFollower> findByUserIdAndVenueId(Long userId, Long venueId);

    void deleteByUserIdAndVenueId(Long userId, Long venueId);
}