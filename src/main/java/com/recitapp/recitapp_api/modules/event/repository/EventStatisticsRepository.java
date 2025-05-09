package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.EventStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventStatisticsRepository extends JpaRepository<EventStatistics, Long> {

    /**
     * Encuentra estadísticas de eventos por ID de evento
     */
    EventStatistics findByEventId(Long eventId);

    /**
     * Cuenta el número total de tickets para un evento
     */
    @Query("SELECT COALESCE(es.totalTickets, 0) FROM EventStatistics es WHERE es.event.id = :eventId")
    Integer countTotalTicketsByEventId(@Param("eventId") Long eventId);

    /**
     * Cuenta el número de tickets vendidos para un evento
     */
    @Query("SELECT COALESCE(es.soldTickets, 0) FROM EventStatistics es WHERE es.event.id = :eventId")
    Integer countSoldTicketsByEventId(@Param("eventId") Long eventId);

    /**
     * Obtiene los eventos más populares (con más tickets vendidos) en un período de tiempo
     */
    @Query("SELECT es FROM EventStatistics es " +
            "JOIN es.event e " +
            "WHERE e.startDateTime BETWEEN :startDate AND :endDate " +
            "ORDER BY es.soldTickets DESC")
    List<EventStatistics> findMostPopularEvents(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Calcula los ingresos totales de todos los eventos en un período de tiempo
     */
    @Query("SELECT SUM(es.totalRevenue) FROM EventStatistics es " +
            "JOIN es.event e " +
            "WHERE e.startDateTime BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}