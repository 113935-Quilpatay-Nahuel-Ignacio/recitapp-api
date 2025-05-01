package com.recitapp.recitapp_api.modules.artist.controller;

import com.recitapp.recitapp_api.modules.artist.dto.ArtistFollowDTO;
import com.recitapp.recitapp_api.modules.artist.dto.ArtistFollowerDTO;
import com.recitapp.recitapp_api.modules.artist.service.ArtistFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/artists")
@RequiredArgsConstructor
public class ArtistFollowController {

    private final ArtistFollowService artistFollowService;

    @PostMapping("/follow")
    public ResponseEntity<Void> followArtist(
            @PathVariable Long userId,
            @RequestBody ArtistFollowDTO followDTO) {
        artistFollowService.followArtist(userId, followDTO.getArtistId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{artistId}/unfollow")
    public ResponseEntity<Void> unfollowArtist(
            @PathVariable Long userId,
            @PathVariable Long artistId) {
        artistFollowService.unfollowArtist(userId, artistId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/following")
    public ResponseEntity<List<ArtistFollowerDTO>> getUserFollowedArtists(
            @PathVariable Long userId) {
        List<ArtistFollowerDTO> followedArtists = artistFollowService.getUserFollowedArtists(userId);
        return ResponseEntity.ok(followedArtists);
    }

    @GetMapping("/{artistId}/is-following")
    public ResponseEntity<Boolean> isFollowingArtist(
            @PathVariable Long userId,
            @PathVariable Long artistId) {
        boolean isFollowing = artistFollowService.isFollowingArtist(userId, artistId);
        return ResponseEntity.ok(isFollowing);
    }
}