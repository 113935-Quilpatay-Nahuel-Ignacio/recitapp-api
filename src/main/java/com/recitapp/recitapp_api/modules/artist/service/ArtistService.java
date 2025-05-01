package com.recitapp.recitapp_api.modules.artist.service;

import com.recitapp.recitapp_api.modules.artist.dto.*;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;

import java.util.List;

public interface ArtistService {

    ArtistDTO createArtist(ArtistDTO artistDTO);

    ArtistDTO updateArtist(Long id, ArtistDTO artistDTO);

    void deleteArtist(Long id);

    ArtistDTO deactivateArtist(Long id);

    ArtistDetailDTO getArtistDetail(Long id);

    List<ArtistDTO> getAllArtists(Boolean activeOnly);

    List<ArtistDTO> searchArtistsByName(String name, Boolean activeOnly);

    List<ArtistDTO> getArtistsByGenre(Long genreId, Boolean activeOnly);

    List<EventDTO> getArtistEvents(Long artistId, Boolean includePastEvents);

    ArtistStatisticsDTO getArtistStatistics(Long artistId);

    List<ArtistDTO> getMostPopularArtists(int limit);

    ArtistDTO updateArtistPlatforms(Long id, ArtistUpdatePlataformDTO platformsData);

    void addGenreToArtist(Long artistId, Long genreId);

    void removeGenreFromArtist(Long artistId, Long genreId);

    List<MusicGenreDTO> getArtistGenres(Long artistId);

    boolean existsById(Long id);

    boolean existsByName(String name);
}