package com.recitapp.recitapp_api.modules.artist.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "profile_image", length = 2000)
    private String profileImage;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "soundcloud_url", length = 500)
    private String soundcloudUrl;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "bandcamp_url", length = 500)
    private String bandcampUrl;

    @ManyToOne
    @JoinColumn(name = "registrar_id")
    private User registrar;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}