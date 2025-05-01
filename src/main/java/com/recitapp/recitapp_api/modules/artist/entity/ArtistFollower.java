package com.recitapp.recitapp_api.modules.artist.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "artist_followers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArtistFollower {
    @EmbeddedId
    private ArtistFollowerId id = new ArtistFollowerId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @Column(name = "follow_date")
    private LocalDateTime followDate;

    @PrePersist
    protected void onCreate() {
        if (followDate == null) {
            followDate = LocalDateTime.now();
        }
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistFollowerId implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "artist_id")
        private Long artistId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ArtistFollowerId that = (ArtistFollowerId) o;
            return Objects.equals(userId, that.userId) &&
                    Objects.equals(artistId, that.artistId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, artistId);
        }
    }
}