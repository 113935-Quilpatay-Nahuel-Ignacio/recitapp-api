package com.recitapp.recitapp_api.modules.notification.repository;

import com.recitapp.recitapp_api.modules.notification.entity.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {

    List<NotificationHistory> findByUserIdOrderBySentAtDesc(Long userId);

    List<NotificationHistory> findByUserIdAndSentAtBetweenOrderBySentAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<NotificationHistory> findByUserIdAndReadAtIsNullOrderBySentAtDesc(Long userId);

    List<NotificationHistory> findByUserIdAndReadAtIsNotNull(Long userId);

    Long countByUserIdAndReadAtIsNull(Long userId);

    @Query("SELECT n FROM NotificationHistory n WHERE n.user.id = :userId AND n.sentAt >= :startDate " +
            "ORDER BY n.sentAt DESC")
    List<NotificationHistory> findRecentNotifications(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDateTime startDate);

    List<NotificationHistory> findByRelatedEventIdOrderBySentAtDesc(Long eventId);

    List<NotificationHistory> findByRelatedArtistIdOrderBySentAtDesc(Long artistId);

    List<NotificationHistory> findByRelatedVenueIdOrderBySentAtDesc(Long venueId);
}