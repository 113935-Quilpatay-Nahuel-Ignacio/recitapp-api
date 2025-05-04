package com.recitapp.recitapp_api.modules.artist.service;

import com.recitapp.recitapp_api.modules.artist.dto.MusicGenreDTO;
import java.util.List;

public interface MusicGenreService {
    List<MusicGenreDTO> getAllGenres();
    MusicGenreDTO getGenreById(Long id);
    MusicGenreDTO createGenre(MusicGenreDTO genreDTO);
    MusicGenreDTO updateGenre(Long id, MusicGenreDTO genreDTO);
    void deleteGenre(Long id);
}