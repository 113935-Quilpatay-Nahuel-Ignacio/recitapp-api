package com.recitapp.recitapp_api.modules.artist.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "music_genres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}