package com.recitapp.recitapp_api.modules.venue.service;

import com.recitapp.recitapp_api.modules.venue.dto.VenueFollowerDTO;
import java.util.List;

public interface VenueFollowerService {
    List<VenueFollowerDTO> getVenuesFollowedByUser(Long userId);
    void followVenue(Long userId, Long venueId);
    void unfollowVenue(Long userId, Long venueId);
    boolean isUserFollowingVenue(Long userId, Long venueId);
}