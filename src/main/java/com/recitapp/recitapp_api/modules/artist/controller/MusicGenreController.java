package com.recitapp.recitapp_api.modules.artist.controller;

import com.recitapp.recitapp_api.modules.artist.dto.MusicGenreDTO;
import com.recitapp.recitapp_api.modules.artist.service.MusicGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class MusicGenreController {

    private final MusicGenreService musicGenreService;

    @GetMapping
    public ResponseEntity<List<MusicGenreDTO>> getAllGenres() {
        List<MusicGenreDTO> genres = musicGenreService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicGenreDTO> getGenreById(@PathVariable Long id) {
        MusicGenreDTO genre = musicGenreService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    @PostMapping
    public ResponseEntity<MusicGenreDTO> createGenre(@RequestBody MusicGenreDTO genreDTO) {
        MusicGenreDTO createdGenre = musicGenreService.createGenre(genreDTO);
        return ResponseEntity.ok(createdGenre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MusicGenreDTO> updateGenre(@PathVariable Long id, @RequestBody MusicGenreDTO genreDTO) {
        MusicGenreDTO updatedGenre = musicGenreService.updateGenre(id, genreDTO);
        return ResponseEntity.ok(updatedGenre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        musicGenreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}