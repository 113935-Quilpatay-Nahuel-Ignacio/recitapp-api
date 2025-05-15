package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    // Find active promotions for an event
    List<Promotion> findByEventIdAndActiveTrue(Long eventId);

    // Find active promotions for an event that are valid at a specific date
    @Query("SELECT p FROM Promotion p WHERE p.event.id = :eventId AND p.active = true " +
            "AND p.startDate <= :currentDate AND p.endDate >= :currentDate")
    List<Promotion> findActivePromotionsByEventIdAndDate(
            @Param("eventId") Long eventId,
            @Param("currentDate") LocalDateTime currentDate);

    // Find by promotion code
    Optional<Promotion> findByPromotionCode(String promotionCode);

    // Find by promotion code and event ID
    Optional<Promotion> findByPromotionCodeAndEventId(String promotionCode, Long eventId);

    // Check if a valid promotion exists for an event and minimum quantity
    @Query("SELECT COUNT(p) > 0 FROM Promotion p WHERE p.event.id = :eventId " +
            "AND p.active = true AND p.minimumQuantity <= :quantity " +
            "AND p.startDate <= :currentDate AND p.endDate >= :currentDate")
    boolean existsValidPromotionForEventAndQuantity(
            @Param("eventId") Long eventId,
            @Param("quantity") Integer quantity,
            @Param("currentDate") LocalDateTime currentDate);
}