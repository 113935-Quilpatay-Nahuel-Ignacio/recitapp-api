package com.recitapp.recitapp_api.modules.artist.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistFollowDTO {
    private Long artistId;
}

