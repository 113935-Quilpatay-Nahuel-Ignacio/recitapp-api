package com.recitapp.recitapp_api.modules.artist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistUpdatePlataformDTO {
    private String spotifyUrl;
    private String youtubeUrl;
    private String soundcloudUrl;
    private String instagramUrl;
    private String bandcampUrl;
}
