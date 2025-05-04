package com.recitapp.recitapp_api.modules.venue.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.venue.dto.VenueFollowerDTO;
import com.recitapp.recitapp_api.modules.venue.entity.Venue;
import com.recitapp.recitapp_api.modules.venue.entity.VenueFollower;
import com.recitapp.recitapp_api.modules.venue.entity.VenueFollowerId;
import com.recitapp.recitapp_api.modules.venue.repository.VenueFollowerRepository;
import com.recitapp.recitapp_api.modules.venue.repository.VenueRepository;
import com.recitapp.recitapp_api.modules.venue.service.VenueFollowerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueFollowerServiceImpl implements VenueFollowerService {

    private final VenueFollowerRepository venueFollowerRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    @Override
    public List<VenueFollowerDTO> getVenuesFollowedByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RecitappException("Usuario no encontrado con ID: " + userId);
        }

        List<VenueFollower> venueFollowers = venueFollowerRepository.findAllByUserId(userId);

        return venueFollowers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void followVenue(Long userId, Long venueId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + userId));

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RecitappException("Recinto no encontrado con ID: " + venueId));

        if (venueFollowerRepository.existsByUserIdAndVenueId(userId, venueId)) {
            throw new RecitappException("El usuario ya sigue a este recinto");
        }

        VenueFollowerId id = new VenueFollowerId(userId, venueId);

        VenueFollower venueFollower = new VenueFollower();
        venueFollower.setId(id);
        venueFollower.setUser(user);
        venueFollower.setVenue(venue);
        venueFollower.setFollowDate(LocalDateTime.now());

        venueFollowerRepository.save(venueFollower);
    }

    @Override
    @Transactional
    public void unfollowVenue(Long userId, Long venueId) {
        if (!userRepository.existsById(userId)) {
            throw new RecitappException("Usuario no encontrado con ID: " + userId);
        }

        if (!venueRepository.existsById(venueId)) {
            throw new RecitappException("Recinto no encontrado con ID: " + venueId);
        }

        VenueFollower venueFollower = venueFollowerRepository.findByUserIdAndVenueId(userId, venueId)
                .orElseThrow(() -> new RecitappException("El usuario no sigue a este recinto"));

        venueFollowerRepository.delete(venueFollower);
    }

    @Override
    public boolean isUserFollowingVenue(Long userId, Long venueId) {
        if (!userRepository.existsById(userId)) {
            throw new RecitappException("Usuario no encontrado con ID: " + userId);
        }

        if (!venueRepository.existsById(venueId)) {
            throw new RecitappException("Recinto no encontrado con ID: " + venueId);
        }

        return venueFollowerRepository.existsByUserIdAndVenueId(userId, venueId);
    }

    private VenueFollowerDTO mapToDTO(VenueFollower venueFollower) {
        return VenueFollowerDTO.builder()
                .venueId(venueFollower.getVenue().getId())
                .venueName(venueFollower.getVenue().getName())
                .venueAddress(venueFollower.getVenue().getAddress())
                .venueImage(venueFollower.getVenue().getImage())
                .followDate(venueFollower.getFollowDate())
                .build();
    }
}