package com.recitapp.recitapp_api.modules.artist.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.artist.dto.*;
import com.recitapp.recitapp_api.modules.artist.entity.Artist;
import com.recitapp.recitapp_api.modules.artist.entity.ArtistGenre;
import com.recitapp.recitapp_api.modules.artist.entity.ArtistStatistics;
import com.recitapp.recitapp_api.modules.artist.entity.MusicGenre;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistFollowerRepository;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistRepository;
import com.recitapp.recitapp_api.modules.artist.repository.ArtistStatisticsRepository;
import com.recitapp.recitapp_api.modules.artist.repository.MusicGenreRepository;
import com.recitapp.recitapp_api.modules.artist.service.ArtistService;
import com.recitapp.recitapp_api.modules.event.dto.EventDTO;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.entity.EventArtist;
import com.recitapp.recitapp_api.modules.event.repository.EventArtistRepository;
import com.recitapp.recitapp_api.modules.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistStatisticsRepository artistStatisticsRepository;
    private final MusicGenreRepository musicGenreRepository;
    private final ArtistFollowerRepository artistFollowerRepository;
    private final EventArtistRepository eventArtistRepository;
    private final EventRepository eventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ArtistDTO createArtist(ArtistDTO artistDTO) {
        if (artistRepository.findByName(artistDTO.getName()).isPresent()) {
            throw new RecitappException("Ya existe un artista con el nombre: " + artistDTO.getName());
        }

        Artist artist = mapToEntity(artistDTO);
        artist.setActive(true);
        Artist savedArtist = artistRepository.save(artist);

        initializeArtistStatistics(savedArtist);

        if (artistDTO.getGenreIds() != null && !artistDTO.getGenreIds().isEmpty()) {
            artistDTO.getGenreIds().forEach(genreId -> addGenreToArtist(savedArtist.getId(), genreId));
        }

        return mapToDTO(savedArtist);
    }

    @Override
    @Transactional
    public ArtistDTO updateArtist(Long id, ArtistDTO artistDTO) {
        Artist artist = findArtistById(id);

        if (artistDTO.getName() != null && !artistDTO.getName().equals(artist.getName()) &&
                artistRepository.findByName(artistDTO.getName()).isPresent()) {
            throw new RecitappException("Ya existe otro artista con el nombre: " + artistDTO.getName());
        }

        if (artistDTO.getName() != null) {
            artist.setName(artistDTO.getName());
        }
        if (artistDTO.getBiography() != null) {
            artist.setBiography(artistDTO.getBiography());
        }
        if (artistDTO.getProfileImage() != null) {
            artist.setProfileImage(artistDTO.getProfileImage());
        }

        Artist updatedArtist = artistRepository.save(artist);
        return mapToDTO(updatedArtist);
    }

    @Override
    @Transactional
    public void deleteArtist(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new RecitappException("Artista no encontrado con ID: " + id);
        }

        artistRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ArtistDTO deactivateArtist(Long id) {
        Artist artist = findArtistById(id);
        artist.setActive(false);
        Artist updatedArtist = artistRepository.save(artist);
        return mapToDTO(updatedArtist);
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistDetailDTO getArtistDetail(Long id) {
        Artist artist = findArtistById(id);

        List<MusicGenreDTO> genres = getArtistGenres(id);

        Long followerCount = artistStatisticsRepository.countFollowersForArtist(id);

        Long upcomingEventsCount = countUpcomingEvents(id);
        Long pastEventsCount = countPastEvents(id);

        return ArtistDetailDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .biography(artist.getBiography())
                .profileImage(artist.getProfileImage())
                .spotifyUrl(artist.getSpotifyUrl())
                .youtubeUrl(artist.getYoutubeUrl())
                .soundcloudUrl(artist.getSoundcloudUrl())
                .instagramUrl(artist.getInstagramUrl())
                .bandcampUrl(artist.getBandcampUrl())
                .registrationDate(artist.getRegistrationDate())
                .updatedAt(artist.getUpdatedAt())
                .active(artist.getActive())
                .genres(genres)
                .followerCount(followerCount)
                .upcomingEventsCount(upcomingEventsCount)
                .pastEventsCount(pastEventsCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistDTO> getAllArtists(Boolean activeOnly) {
        List<Artist> artists;
        if (activeOnly) {
            artists = artistRepository.findByActiveTrue();
        } else {
            artists = artistRepository.findAll();
        }
        return artists.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistDTO> searchArtistsByName(String name, Boolean activeOnly) {
        List<Artist> artists;
        if (activeOnly) {
            artists = artistRepository.findByActiveTrueAndNameContainingIgnoreCase(name);
        } else {
            artists = artistRepository.findByNameContainingIgnoreCase(name);
        }
        return artists.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistDTO> getArtistsByGenre(Long genreId, Boolean activeOnly) {
        if (!musicGenreRepository.existsById(genreId)) {
            throw new RecitappException("Género musical no encontrado con ID: " + genreId);
        }

        List<Artist> artists = artistRepository.findByGenreId(genreId);
        if (activeOnly) {
            artists = artists.stream().filter(Artist::getActive).toList();
        }

        return artists.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDTO> getArtistEvents(Long artistId, Boolean includePastEvents) {
        findArtistById(artistId);

        LocalDateTime now = LocalDateTime.now();
        List<EventDTO> events = new ArrayList<>();

        // Get events where the artist is in the EventArtist table
        List<EventArtist> eventArtists;
        if (includePastEvents) {
            eventArtists = eventArtistRepository.findByArtistId(artistId);
        } else {
            eventArtists = eventArtistRepository.findUpcomingEventsByArtistId(artistId, now);
        }

        events.addAll(eventArtists.stream()
                .map(ea -> mapEventToDTO(ea.getEvent()))
                .collect(Collectors.toList()));

        // Get events where the artist is the main artist
        List<Event> mainArtistEvents;
        if (includePastEvents) {
            mainArtistEvents = eventRepository.findByMainArtistId(artistId);
        } else {
            mainArtistEvents = eventRepository.findByMainArtistIdAndStartDateTimeAfter(artistId, now);
        }

        events.addAll(mainArtistEvents.stream()
                .map(this::mapEventToDTO)
                .collect(Collectors.toList()));

        // Remove duplicates (in case an artist is both main artist and in EventArtist table)
        return events.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistStatisticsDTO getArtistStatistics(Long artistId) {
        Artist artist = findArtistById(artistId);

        ArtistStatistics stats = artistStatisticsRepository.findByArtistId(artistId)
                .orElseGet(() -> initializeArtistStatistics(artist));

        Long followerCount = artistStatisticsRepository.countFollowersForArtist(artistId);
        Long upcomingEventsCount = countUpcomingEvents(artistId);
        Long pastEventsCount = countPastEvents(artistId);

        Float followerGrowthRate = 0.0f;

        return ArtistStatisticsDTO.builder()
                .artistId(artist.getId())
                .artistName(artist.getName())
                .profileImage(artist.getProfileImage())
                .totalFollowers(followerCount.intValue())
                .totalEvents(upcomingEventsCount + pastEventsCount)
                .upcomingEvents(upcomingEventsCount.intValue())
                .pastEvents(pastEventsCount.intValue())
                .lastUpdateDate(stats.getUpdateDate())
                .followerGrowthRate(followerGrowthRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArtistDTO> getMostPopularArtists(int limit) {
        List<Artist> popularArtists = artistRepository.findMostPopularArtists();

        if (popularArtists.size() > limit) {
            popularArtists = popularArtists.subList(0, limit);
        }

        return popularArtists.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArtistDTO updateArtistPlatforms(Long id, ArtistUpdatePlataformDTO platformsData) {
        Artist artist = findArtistById(id);

        if (platformsData.getSpotifyUrl() != null) {
            artist.setSpotifyUrl(platformsData.getSpotifyUrl());
        }
        if (platformsData.getYoutubeUrl() != null) {
            artist.setYoutubeUrl(platformsData.getYoutubeUrl());
        }
        if (platformsData.getSoundcloudUrl() != null) {
            artist.setSoundcloudUrl(platformsData.getSoundcloudUrl());
        }
        if (platformsData.getInstagramUrl() != null) {
            artist.setInstagramUrl(platformsData.getInstagramUrl());
        }
        if (platformsData.getBandcampUrl() != null) {
            artist.setBandcampUrl(platformsData.getBandcampUrl());
        }

        Artist updatedArtist = artistRepository.save(artist);
        return mapToDTO(updatedArtist);
    }

    @Override
    @Transactional
    public void addGenreToArtist(Long artistId, Long genreId) {
        Artist artist = findArtistById(artistId);
        MusicGenre genre = findGenreById(genreId);

        String jpql = "SELECT COUNT(ag) FROM ArtistGenre ag WHERE ag.artist.id = :artistId AND ag.genre.id = :genreId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("artistId", artistId)
                .setParameter("genreId", genreId)
                .getSingleResult();

        if (count > 0) {
            throw new RecitappException("El artista ya tiene asociado este género musical");
        }

        ArtistGenre artistGenre = new ArtistGenre();

        artistGenre.getId().setArtistId(artistId);
        artistGenre.getId().setGenreId(genreId);

        artistGenre.setArtist(artist);
        artistGenre.setGenre(genre);

        entityManager.persist(artistGenre);
    }

    @Override
    @Transactional
    public void removeGenreFromArtist(Long artistId, Long genreId) {
        String jpql = "SELECT ag FROM ArtistGenre ag WHERE ag.artist.id = :artistId AND ag.genre.id = :genreId";
        List<ArtistGenre> result = entityManager.createQuery(jpql, ArtistGenre.class)
                .setParameter("artistId", artistId)
                .setParameter("genreId", genreId)
                .getResultList();

        if (result.isEmpty()) {
            throw new RecitappException("El artista no tiene asociado este género musical");
        }

        entityManager.remove(result.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MusicGenreDTO> getArtistGenres(Long artistId) {
        findArtistById(artistId);

        List<MusicGenre> genres = musicGenreRepository.findGenresByArtistId(artistId);
        return genres.stream().map(this::mapGenreToDTO).collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return artistRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return artistRepository.findByName(name).isPresent();
    }

    private Artist findArtistById(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Artista no encontrado con ID: " + id));
    }

    private MusicGenre findGenreById(Long id) {
        return musicGenreRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Género musical no encontrado con ID: " + id));
    }

    private ArtistStatistics initializeArtistStatistics(Artist artist) {
        ArtistStatistics stats = new ArtistStatistics();
        stats.setArtist(artist);
        stats.setTotalFollowers(0);
        stats.setTotalEvents(0);
        stats.setUpdateDate(LocalDateTime.now());
        return artistStatisticsRepository.save(stats);
    }

    private Long countUpcomingEvents(Long artistId) {
        LocalDateTime now = LocalDateTime.now();
        return eventArtistRepository.countUpcomingEventsByArtistId(artistId, now);
    }

    private Long countPastEvents(Long artistId) {
        LocalDateTime now = LocalDateTime.now();
        return eventArtistRepository.countPastEventsByArtistId(artistId, now);
    }

    private Artist mapToEntity(ArtistDTO dto) {
        Artist artist = new Artist();
        artist.setName(dto.getName());
        artist.setBiography(dto.getBiography());
        artist.setProfileImage(dto.getProfileImage());
        artist.setSpotifyUrl(dto.getSpotifyUrl());
        artist.setYoutubeUrl(dto.getYoutubeUrl());
        artist.setSoundcloudUrl(dto.getSoundcloudUrl());
        artist.setInstagramUrl(dto.getInstagramUrl());
        artist.setBandcampUrl(dto.getBandcampUrl());
        artist.setActive(dto.getActive() != null ? dto.getActive() : true);
        return artist;
    }

    private ArtistDTO mapToDTO(Artist entity) {
        return ArtistDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .biography(entity.getBiography())
                .profileImage(entity.getProfileImage())
                .spotifyUrl(entity.getSpotifyUrl())
                .youtubeUrl(entity.getYoutubeUrl())
                .soundcloudUrl(entity.getSoundcloudUrl())
                .instagramUrl(entity.getInstagramUrl())
                .bandcampUrl(entity.getBandcampUrl())
                .active(entity.getActive())
                .build();
    }

    private MusicGenreDTO mapGenreToDTO(MusicGenre genre) {
        return MusicGenreDTO.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();
    }

    private EventDTO mapEventToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .venueId(event.getVenue().getId())
                .venueName(event.getVenue().getName())
                .mainArtistId(event.getMainArtist() != null ? event.getMainArtist().getId() : null)
                .mainArtistName(event.getMainArtist() != null ? event.getMainArtist().getName() : null)
                .statusName(event.getStatus().getName())
                .flyerImage(event.getFlyerImage())
                .build();
    }
}