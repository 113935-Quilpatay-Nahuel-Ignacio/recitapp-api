package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.modules.event.dto.EventDTO;

import java.util.List;

/**
 * Servicio para generar recomendaciones personalizadas de eventos para usuarios
 */
public interface UserRecommendationService {

    /**
     * Genera recomendaciones personalizadas de eventos basadas en los artistas y eventos seguidos
     * 
     * @param userId ID del usuario
     * @param limit Límite de recomendaciones
     * @return Lista de eventos recomendados
     */
    List<EventDTO> getPersonalizedEventRecommendations(Long userId, int limit);

    /**
     * Obtiene eventos de artistas que sigue el usuario
     * 
     * @param userId ID del usuario
     * @param limit Límite de eventos
     * @return Lista de eventos de artistas seguidos
     */
    List<EventDTO> getEventsFromFollowedArtists(Long userId, int limit);

    /**
     * Obtiene recomendaciones de eventos similares basadas en el historial de compras
     * 
     * @param userId ID del usuario
     * @param limit Límite de recomendaciones
     * @return Lista de eventos similares
     */
    List<EventDTO> getSimilarEventRecommendations(Long userId, int limit);
} 