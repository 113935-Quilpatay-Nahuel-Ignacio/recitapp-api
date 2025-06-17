package com.recitapp.recitapp_api.modules.user.service.impl;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import com.recitapp.recitapp_api.modules.event.service.EventService;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.service.UserRecommendationService;
import com.recitapp.recitapp_api.modules.user.dto.EventRecommendationDTO;
import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRecommendationServiceImpl implements UserRecommendationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Override
    public List<EventDTO> getPersonalizedEventRecommendations(Long userId, int limit) {
        // Verificar que el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Set<EventDTO> recommendations = new LinkedHashSet<>();

        try {
            // 1. Obtener eventos de artistas seguidos (70% del peso)
            List<EventDTO> followedArtistsEvents = getEventsFromFollowedArtists(userId, (int) (limit * 0.7));
            recommendations.addAll(followedArtistsEvents);

            // 2. Obtener eventos similares basados en historial (30% del peso)
            List<EventDTO> similarEvents = getSimilarEventRecommendations(userId, (int) (limit * 0.3));
            recommendations.addAll(similarEvents);

            // 3. Si no hay suficientes recomendaciones, completar con eventos populares
            if (recommendations.size() < limit) {
                List<EventDTO> popularEvents = getPopularEvents(userId, limit - recommendations.size());
                recommendations.addAll(popularEvents);
            }

        } catch (Exception e) {
            log.error("Error generating personalized recommendations for user {}: {}", userId, e.getMessage());
            // Fallback: devolver eventos populares
            return getPopularEvents(userId, limit);
        }

        return recommendations.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventDTO> getEventsFromFollowedArtists(Long userId, int limit) {
        try {
            // Obtener IDs de artistas seguidos por el usuario
            List<Long> followedArtistIds = userRepository.findFollowedArtistIds(userId);
            
            if (followedArtistIds.isEmpty()) {
                log.info("User {} doesn't follow any artists", userId);
                return new ArrayList<>();
            }

            // Obtener eventos futuros de esos artistas
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, limit);
            
            List<Event> events = eventRepository.findByPrimaryArtistIdInAndStartDateTimeAfterOrderByStartDateTimeAsc(
                    followedArtistIds, now, pageable);

            return events.stream()
                    .map(eventService::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting events from followed artists for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<EventDTO> getSimilarEventRecommendations(Long userId, int limit) {
        try {
            // Obtener géneros musicales de eventos a los que ha asistido el usuario
            List<String> userGenres = userRepository.findUserPreferredGenres(userId);
            
            if (userGenres.isEmpty()) {
                log.info("User {} has no purchase history to base recommendations on", userId);
                return new ArrayList<>();
            }

            // Obtener eventos futuros con géneros similares
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, limit);
            
            List<Event> events = eventRepository.findByGenreNamesInAndStartDateTimeAfterOrderByStartDateTimeAsc(
                    userGenres, now, pageable);

            // Filtrar eventos a los que ya asistió o tiene tickets
            List<Long> userEventIds = userRepository.findUserEventIds(userId);
            
            return events.stream()
                    .filter(event -> !userEventIds.contains(event.getId()))
                    .map(eventService::convertToDTO)
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting similar event recommendations for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene eventos populares como fallback cuando no hay suficientes recomendaciones personalizadas
     */
    private List<EventDTO> getPopularEvents(Long userId, int limit) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, limit);
            
            // Obtener eventos futuros ordenados por cantidad de tickets vendidos
            List<Event> popularEvents = eventRepository.findPopularUpcomingEvents(now, pageable);
            
            // Filtrar eventos a los que ya asistió
            List<Long> userEventIds = userRepository.findUserEventIds(userId);
            
            return popularEvents.stream()
                    .filter(event -> !userEventIds.contains(event.getId()))
                    .map(eventService::convertToDTO)
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting popular events for user {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<EventRecommendationDTO> getEnhancedPersonalizedRecommendations(Long userId, int limit) {
        // Verificar que el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        List<EventRecommendationDTO> recommendations = new ArrayList<>();

        try {
            // Obtener información de seguimientos del usuario
            List<String> followedArtistNames = userRepository.findFollowedArtistNames(userId);
            List<String> followedVenueNames = userRepository.findFollowedVenueNames(userId);
            List<Long> followedArtistIds = userRepository.findFollowedArtistIds(userId);
            List<Long> followedVenueIds = userRepository.findFollowedVenueIds(userId);
            List<String> userGenres = userRepository.findUserPreferredGenres(userId);
            List<Long> userEventIds = userRepository.findUserEventIds(userId);

            // 1. Eventos de artistas seguidos
            if (!followedArtistIds.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                Pageable pageable = PageRequest.of(0, (int) (limit * 0.6));
                
                List<Event> artistEvents = eventRepository.findByPrimaryArtistIdInAndStartDateTimeAfterOrderByStartDateTimeAsc(
                        followedArtistIds, now, pageable);

                for (Event event : artistEvents) {
                    if (!userEventIds.contains(event.getId())) {
                        EventRecommendationDTO recommendation = convertToRecommendationDTO(event, "FOLLOWED_ARTIST");
                        
                        // Determinar qué artistas seguidos están en este evento
                        List<String> matchingArtists = new ArrayList<>();
                        if (event.getMainArtist() != null && followedArtistNames.contains(event.getMainArtist().getName())) {
                            matchingArtists.add(event.getMainArtist().getName());
                        }
                        
                        recommendation.setFollowedArtistNames(matchingArtists);
                        recommendation.setRecommendationReason(buildArtistRecommendationReason(matchingArtists));
                        recommendation.setRecommendationScore(0.9);
                        
                        recommendations.add(recommendation);
                    }
                }
            }

            // 2. Eventos en recintos seguidos
            if (!followedVenueIds.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                Pageable pageable = PageRequest.of(0, (int) (limit * 0.3));
                
                List<Event> venueEvents = eventRepository.findByVenueIdInAndStartDateTimeAfterOrderByStartDateTimeAsc(
                        followedVenueIds, now, pageable);

                for (Event event : venueEvents) {
                    if (!userEventIds.contains(event.getId()) && 
                        recommendations.stream().noneMatch(r -> r.getId().equals(event.getId()))) {
                        
                        EventRecommendationDTO recommendation = convertToRecommendationDTO(event, "FOLLOWED_VENUE");
                        
                        // Determinar qué recintos seguidos están en este evento
                        List<String> matchingVenues = new ArrayList<>();
                        if (event.getVenue() != null && followedVenueNames.contains(event.getVenue().getName())) {
                            matchingVenues.add(event.getVenue().getName());
                        }
                        
                        recommendation.setFollowedVenueNames(matchingVenues);
                        recommendation.setRecommendationReason(buildVenueRecommendationReason(matchingVenues));
                        recommendation.setRecommendationScore(0.7);
                        
                        recommendations.add(recommendation);
                    }
                }
            }

            // 3. Eventos por géneros similares
            if (!userGenres.isEmpty() && recommendations.size() < limit) {
                LocalDateTime now = LocalDateTime.now();
                Pageable pageable = PageRequest.of(0, limit - recommendations.size());
                
                List<Event> genreEvents = eventRepository.findByGenreNamesInAndStartDateTimeAfterOrderByStartDateTimeAsc(
                        userGenres, now, pageable);

                for (Event event : genreEvents) {
                    if (!userEventIds.contains(event.getId()) && 
                        recommendations.stream().noneMatch(r -> r.getId().equals(event.getId()))) {
                        
                        EventRecommendationDTO recommendation = convertToRecommendationDTO(event, "SIMILAR_GENRE");
                        recommendation.setMatchingGenres(userGenres);
                        recommendation.setRecommendationReason("Basado en tus gustos musicales y eventos similares");
                        recommendation.setRecommendationScore(0.5);
                        
                        recommendations.add(recommendation);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error generating enhanced recommendations for user {}: {}", userId, e.getMessage());
        }

        return recommendations.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private EventRecommendationDTO convertToRecommendationDTO(Event event, String recommendationType) {
        return EventRecommendationDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .venueId(event.getVenue() != null ? event.getVenue().getId() : null)
                .venueName(event.getVenue() != null ? event.getVenue().getName() : null)
                .venueAddress(event.getVenue() != null ? event.getVenue().getAddress() : null)
                .mainArtistId(event.getMainArtist() != null ? event.getMainArtist().getId() : null)
                .mainArtistName(event.getMainArtist() != null ? event.getMainArtist().getName() : null)
                .mainArtistImage(event.getMainArtist() != null ? event.getMainArtist().getProfileImage() : null)
                .statusName(event.getStatus() != null ? event.getStatus().getName() : null)
                .flyerImage(event.getFlyerImage())
                .recommendationType(recommendationType)
                .followedArtistNames(new ArrayList<>())
                .followedVenueNames(new ArrayList<>())
                .matchingGenres(new ArrayList<>())
                .build();
    }

    private String buildArtistRecommendationReason(List<String> artistNames) {
        if (artistNames.isEmpty()) {
            return "Recomendado para ti";
        } else if (artistNames.size() == 1) {
            return "Sigues al artista: " + artistNames.get(0);
        } else {
            return "Sigues a los artistas: " + String.join(", ", artistNames);
        }
    }

    private String buildVenueRecommendationReason(List<String> venueNames) {
        if (venueNames.isEmpty()) {
            return "Recomendado para ti";
        } else if (venueNames.size() == 1) {
            return "Sigues el recinto: " + venueNames.get(0);
        } else {
            return "Sigues los recintos: " + String.join(", ", venueNames);
        }
    }
} 