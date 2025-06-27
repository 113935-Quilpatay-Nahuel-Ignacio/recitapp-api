package com.recitapp.recitapp_api.modules.artist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class ArtistDTO {
    private Long id;

    @NotBlank(message = "El nombre del artista es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String name;

    private String biography;
    private String profileImage;
    private String spotifyUrl;
    private String youtubeUrl;
    private String soundcloudUrl;
    private String instagramUrl;
    private String bandcampUrl;
    private Boolean active;
    private List<Long> genreIds;
    private Long registrarId;
    private LocalDateTime registrationDate;
    private LocalDateTime updatedAt;
}