package com.recitapp.recitapp_api.modules.artist.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.artist.dto.ArtistFollowerDTO;
import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import com.recitapp.recitapp_api.modules.artist.entity.ArtistFollower;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistFollowerRepository;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistRepository;
import com.recitapp.recitapp_api.modules.artist.service.ArtistFollowService;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistFollowServiceImpl implements ArtistFollowService {

    private final ArtistFollowerRepository artistFollowerRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @Override
    @Transactional
    public void followArtist(Long userId, Long artistId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + userId));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RecitappException("Artista no encontrado con ID: " + artistId));

        if (artistFollowerRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new RecitappException("El usuario ya sigue a este artista");
        }

        ArtistFollower follower = new ArtistFollower();
        follower.getId().setUserId(userId);
        follower.getId().setArtistId(artistId);
        follower.setUser(user);
        follower.setArtist(artist);
        follower.setFollowDate(LocalDateTime.now());

        artistFollowerRepository.save(follower);
    }

    @Override
    @Transactional
    public void unfollowArtist(Long userId, Long artistId) {
        if (!artistFollowerRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new RecitappException("El usuario no sigue a este artista");
        }

        artistFollowerRepository.deleteByUserIdAndArtistId(userId, artistId);
    }

    @Override
    public List<ArtistFollowerDTO> getUserFollowedArtists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RecitappException("Usuario no encontrado con ID: " + userId);
        }

        List<ArtistFollower> followers = artistFollowerRepository.findByUserIdOrderByFollowDateDesc(userId);

        return followers.stream()
                .map(follower -> new ArtistFollowerDTO(
                        follower.getArtist().getId(),
                        follower.getArtist().getName(),
                        follower.getArtist().getProfileImage(),
                        follower.getFollowDate()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFollowingArtist(Long userId, Long artistId) {
        return artistFollowerRepository.existsByUserIdAndArtistId(userId, artistId);
    }
}