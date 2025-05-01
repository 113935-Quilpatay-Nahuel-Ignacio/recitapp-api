package com.recitapp.recitapp_api.modules.artist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicGenreDTO {
    private Long id;
    private String name;
    private String description;
}