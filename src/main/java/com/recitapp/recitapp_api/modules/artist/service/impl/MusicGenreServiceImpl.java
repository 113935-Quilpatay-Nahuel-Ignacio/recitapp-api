package com.recitapp.recitapp_api.modules.artist.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.artist.dto.MusicGenreDTO;
import com.recitapp.recitapp_api.modules.artist.entity.MusicGenre;
import com.recitapp.recitapp_api.modules.artist.repository.MusicGenreRepository;
import com.recitapp.recitapp_api.modules.artist.service.MusicGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MusicGenreServiceImpl implements MusicGenreService {

    private final MusicGenreRepository musicGenreRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MusicGenreDTO> getAllGenres() {
        List<MusicGenre> genres = musicGenreRepository.findAll();
        return genres.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MusicGenreDTO getGenreById(Long id) {
        MusicGenre genre = findGenreById(id);
        return mapToDTO(genre);
    }

    @Override
    @Transactional
    public MusicGenreDTO createGenre(MusicGenreDTO genreDTO) {
        if (musicGenreRepository.existsByName(genreDTO.getName())) {
            throw new RecitappException("Ya existe un género con el nombre: " + genreDTO.getName());
        }

        MusicGenre genre = new MusicGenre();
        genre.setName(genreDTO.getName());
        genre.setDescription(genreDTO.getDescription());

        MusicGenre savedGenre = musicGenreRepository.save(genre);
        return mapToDTO(savedGenre);
    }

    @Override
    @Transactional
    public MusicGenreDTO updateGenre(Long id, MusicGenreDTO genreDTO) {
        MusicGenre genre = findGenreById(id);

        if (!genre.getName().equals(genreDTO.getName()) &&
                musicGenreRepository.existsByName(genreDTO.getName())) {
            throw new RecitappException("Ya existe otro género con el nombre: " + genreDTO.getName());
        }

        genre.setName(genreDTO.getName());
        genre.setDescription(genreDTO.getDescription());

        MusicGenre updatedGenre = musicGenreRepository.save(genre);
        return mapToDTO(updatedGenre);
    }

    @Override
    @Transactional
    public void deleteGenre(Long id) {
        if (!musicGenreRepository.existsById(id)) {
            throw new RecitappException("Género no encontrado con ID: " + id);
        }

        musicGenreRepository.deleteById(id);
    }

    private MusicGenre findGenreById(Long id) {
        return musicGenreRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Género no encontrado con ID: " + id));
    }

    private MusicGenreDTO mapToDTO(MusicGenre genre) {
        return MusicGenreDTO.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();
    }
}