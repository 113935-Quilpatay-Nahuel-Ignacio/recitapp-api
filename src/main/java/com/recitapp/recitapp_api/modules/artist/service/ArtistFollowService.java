package com.recitapp.recitapp_api.modules.artist.service;

import com.recitapp.recitapp_api.modules.artist.dto.ArtistFollowerDTO;

import java.util.List;

public interface ArtistFollowService {
    void followArtist(Long userId, Long artistId);
    void unfollowArtist(Long userId, Long artistId);
    List<ArtistFollowerDTO> getUserFollowedArtists(Long userId);
    boolean isFollowingArtist(Long userId, Long artistId);
}