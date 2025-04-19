package com.recitapp.recitapp_api.modules.artist.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "artist_followers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistFollower {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "follow_date")
    private LocalDateTime followDate;

    @Embeddable
    public static class ArtistFollowerId implements java.io.Serializable {
        @Column(name = "user_id")
        private Long userId;

        @Column(name = "artist_id")
        private Long artistId;

        // equals and hashCode methods

    }

    @EmbeddedId
    private ArtistFollowerId id = new ArtistFollowerId();

    @PrePersist
    protected void onCreate() {
        followDate = LocalDateTime.now();
    }
}