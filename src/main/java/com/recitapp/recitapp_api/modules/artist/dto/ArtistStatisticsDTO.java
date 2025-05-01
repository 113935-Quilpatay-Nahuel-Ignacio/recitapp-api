package com.recitapp.recitapp_api.modules.artist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistStatisticsDTO {
    private Long artistId;
    private String artistName;
    private String profileImage;
    private Integer totalFollowers;
    private Long totalEvents;
    private Integer upcomingEvents;
    private Integer pastEvents;
    private LocalDateTime lastUpdateDate;
    private Float followerGrowthRate;
}