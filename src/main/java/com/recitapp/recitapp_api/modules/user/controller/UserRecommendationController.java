package com.recitapp.recitapp_api.modules.user.controller;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.user.service.UserRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para generar recomendaciones personalizadas de eventos
 */
@RestController
@RequestMapping("/users/{userId}/recommendations")
@RequiredArgsConstructor
public class UserRecommendationController {

    private final UserRecommendationService userRecommendationService;

    /**
     * Obtiene recomendaciones de eventos basadas en los artistas y eventos que sigue el usuario
     * 
     * @param userId ID del usuario
     * @param limit Límite de recomendaciones (por defecto 20)
     * @return Lista de eventos recomendados
     */
    @GetMapping("/events")
    public ResponseEntity<List<EventDTO>> getEventRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<EventDTO> recommendations = userRecommendationService.getPersonalizedEventRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Obtiene recomendaciones de eventos basadas en artistas seguidos
     * 
     * @param userId ID del usuario
     * @param limit Límite de recomendaciones (por defecto 10)
     * @return Lista de eventos de artistas seguidos
     */
    @GetMapping("/events/followed-artists")
    public ResponseEntity<List<EventDTO>> getEventsFromFollowedArtists(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<EventDTO> recommendations = userRecommendationService.getEventsFromFollowedArtists(userId, limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Obtiene recomendaciones de eventos similares basadas en el historial de compras
     * 
     * @param userId ID del usuario
     * @param limit Límite de recomendaciones (por defecto 10)
     * @return Lista de eventos similares
     */
    @GetMapping("/events/similar")
    public ResponseEntity<List<EventDTO>> getSimilarEventRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<EventDTO> recommendations = userRecommendationService.getSimilarEventRecommendations(userId, limit);
        return ResponseEntity.ok(recommendations);
    }
} 