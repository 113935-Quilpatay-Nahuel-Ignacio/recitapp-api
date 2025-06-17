package com.recitapp.recitapp_api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para recomendaciones de eventos con información detallada sobre por qué se recomienda
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRecommendationDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    
    // Información del recinto
    private Long venueId;
    private String venueName;
    private String venueAddress;
    
    // Información del artista principal
    private Long mainArtistId;
    private String mainArtistName;
    private String mainArtistImage;
    
    // Información adicional del evento
    private String statusName;
    private String flyerImage;
    private Double ticketPrice;
    private Long availableTickets;
    
    // Información de la recomendación
    private String recommendationType; // "FOLLOWED_ARTIST", "FOLLOWED_VENUE", "SIMILAR_GENRE", "POPULAR"
    private Double recommendationScore;
    private List<String> followedArtistNames; // Artistas seguidos que aparecen en este evento
    private List<String> followedVenueNames; // Recintos seguidos relacionados
    private List<String> matchingGenres; // Géneros que coinciden con el historial del usuario
    private String recommendationReason; // Texto explicativo personalizado
} 