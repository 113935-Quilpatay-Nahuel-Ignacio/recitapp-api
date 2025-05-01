package com.recitapp.recitapp_api.modules.artist.repository;

import com.recitapp.recitapp_api.modules.artist.entity.ArtistFollower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArtistFollowerRepository extends JpaRepository<ArtistFollower, ArtistFollower.ArtistFollowerId> {
    boolean existsByUserIdAndArtistId(Long userId, Long artistId);
    void deleteByUserIdAndArtistId(Long userId, Long artistId);
    List<ArtistFollower> findByUserIdOrderByFollowDateDesc(Long userId);
}