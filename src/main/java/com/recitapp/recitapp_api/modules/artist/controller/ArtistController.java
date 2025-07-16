package com.recitapp.recitapp_api.modules.artist.controller;


import com.recitapp.recitapp_api.modules.artist.dto.*;
import com.recitapp.recitapp_api.modules.artist.service.ArtistService;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final UserRepository userRepository;

    /**
     * Obtiene el usuario actual autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Verifica si el usuario actual puede modificar un artista
     */
    private boolean canModifyArtist(User user, Long artistId) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        
        String roleName = user.getRole().getName();
        
        // ADMIN y MODERADOR pueden modificar cualquier artista
        if ("ADMIN".equals(roleName) || "MODERADOR".equals(roleName)) {
            return true;
        }
        
        // REGISTRADOR_EVENTO solo puede modificar artistas que creó
        if ("REGISTRADOR_EVENTO".equals(roleName)) {
            try {
                ArtistDTO artist = artistService.getArtistForEdit(artistId);
                return artist.getRegistrarId() != null && artist.getRegistrarId().equals(user.getId());
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    @PostMapping
    public ResponseEntity<ArtistDTO> createArtist(@Valid @RequestBody ArtistDTO artistDTO,
                                                  @RequestParam(required = false) Long registrarId) {
        User currentUser = getCurrentUser();
        
        // Si no se proporciona registrarId y el usuario es REGISTRADOR_EVENTO, usar su ID
        if (registrarId == null && currentUser != null && 
            "REGISTRADOR_EVENTO".equals(currentUser.getRole().getName())) {
            registrarId = currentUser.getId();
        }
        
        ArtistDTO createdArtist = artistService.createArtist(artistDTO, registrarId);
        return new ResponseEntity<>(createdArtist, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtistDTO> updateArtist(@PathVariable Long id, @Valid @RequestBody ArtistDTO artistDTO) {
        User currentUser = getCurrentUser();
        
        if (!canModifyArtist(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ArtistDTO updatedArtist = artistService.updateArtist(id, artistDTO);
        return ResponseEntity.ok(updatedArtist);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtist(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        
        if (!canModifyArtist(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ArtistDTO> deactivateArtist(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        
        if (!canModifyArtist(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ArtistDTO deactivatedArtist = artistService.deactivateArtist(id);
        return ResponseEntity.ok(deactivatedArtist);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistDetailDTO> getArtistDetail(@PathVariable Long id) {
        ArtistDetailDTO artistDetail = artistService.getArtistDetail(id);
        return ResponseEntity.ok(artistDetail);
    }

    @GetMapping
    public ResponseEntity<List<ArtistDTO>> getAllArtists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long genreId,
            @RequestParam(defaultValue = "true") Boolean activeOnly) {

        List<ArtistDTO> artists;
        if (name != null && !name.isEmpty()) {
            artists = artistService.searchArtistsByName(name, activeOnly);
        } else if (genreId != null) {
            artists = artistService.getArtistsByGenre(genreId, activeOnly);
        } else {
            artists = artistService.getAllArtists(activeOnly);
        }
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<List<EventDTO>> getArtistEvents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") Boolean includePastEvents) {
        List<EventDTO> events = artistService.getArtistEvents(id, includePastEvents);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<ArtistStatisticsDTO> getArtistStatistics(@PathVariable Long id) {
        ArtistStatisticsDTO statistics = artistService.getArtistStatistics(id);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<ArtistDTO>> getMostPopularArtists(
            @RequestParam(defaultValue = "10") int limit) {
        List<ArtistDTO> popularArtists = artistService.getMostPopularArtists(limit);
        return ResponseEntity.ok(popularArtists);
    }

    @PutMapping("/{id}/platforms")
    public ResponseEntity<ArtistDTO> updateArtistPlatforms(
            @PathVariable Long id,
            @Valid @RequestBody ArtistUpdatePlataformDTO platformsData) {
        ArtistDTO updatedArtist = artistService.updateArtistPlatforms(id, platformsData);
        return ResponseEntity.ok(updatedArtist);
    }

    @PostMapping("/{artistId}/genres/{genreId}")
    public ResponseEntity<Void> addGenreToArtist(
            @PathVariable Long artistId,
            @PathVariable Long genreId) {
        artistService.addGenreToArtist(artistId, genreId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{artistId}/genres/{genreId}")
    public ResponseEntity<Void> removeGenreFromArtist(
            @PathVariable Long artistId,
            @PathVariable Long genreId) {
        artistService.removeGenreFromArtist(artistId, genreId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/genres")
    public ResponseEntity<List<MusicGenreDTO>> getArtistGenres(@PathVariable Long id) {
        List<MusicGenreDTO> genres = artistService.getArtistGenres(id);
        return ResponseEntity.ok(genres);
    }
}