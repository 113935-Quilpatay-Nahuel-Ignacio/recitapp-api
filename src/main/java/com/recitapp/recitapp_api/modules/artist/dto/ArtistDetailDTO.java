package com.recitapp.recitapp_api.modules.artist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistDetailDTO {
    private Long id;
    private String name;
    private String biography;
    private String profileImage;
    private String spotifyUrl;
    private String youtubeUrl;
    private String soundcloudUrl;
    private String instagramUrl;
    private String bandcampUrl;
    private LocalDateTime registrationDate;
    private LocalDateTime updatedAt;
    private Boolean active;
    private List<MusicGenreDTO> genres;
    private Long followerCount;
    private Long upcomingEventsCount;
    private Long pastEventsCount;
}