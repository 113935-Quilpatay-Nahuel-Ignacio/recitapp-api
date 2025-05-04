package com.recitapp.recitapp_api.modules.venue.controller;

import com.recitapp.recitapp_api.modules.venue.dto.FollowVenueRequestDTO;
import com.recitapp.recitapp_api.modules.venue.dto.VenueFollowerDTO;
import com.recitapp.recitapp_api.modules.venue.service.VenueFollowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class VenueFollowerController {

    private final VenueFollowerService venueFollowerService;

    @GetMapping("/{userId}/venues/following")
    public ResponseEntity<List<VenueFollowerDTO>> getVenuesFollowedByUser(@PathVariable Long userId) {
        List<VenueFollowerDTO> followedVenues = venueFollowerService.getVenuesFollowedByUser(userId);
        return ResponseEntity.ok(followedVenues);
    }

    @PostMapping("/{userId}/venues/follow")
    public ResponseEntity<Void> followVenue(
            @PathVariable Long userId,
            @Valid @RequestBody FollowVenueRequestDTO request) {
        venueFollowerService.followVenue(userId, request.getVenueId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}/venues/{venueId}/unfollow")
    public ResponseEntity<Void> unfollowVenue(
            @PathVariable Long userId,
            @PathVariable Long venueId) {
        venueFollowerService.unfollowVenue(userId, venueId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/venues/{venueId}/is-following")
    public ResponseEntity<Boolean> isFollowingVenue(
            @PathVariable Long userId,
            @PathVariable Long venueId) {
        boolean isFollowing = venueFollowerService.isUserFollowingVenue(userId, venueId);
        return ResponseEntity.ok(isFollowing);
    }
}