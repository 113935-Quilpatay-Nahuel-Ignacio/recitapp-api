package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.TicketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPriceRepository extends JpaRepository<TicketPrice, Long> {
    
    /**
     * Busca todos los precios de tickets para un evento específico
     */
    List<TicketPrice> findByEventId(Long eventId);
    
    /**
     * Busca todos los precios de tickets para una sección específica
     */
    List<TicketPrice> findBySectionId(Long sectionId);
    
    /**
     * Busca precios de tickets para un evento y sección específicos
     */
    List<TicketPrice> findByEventIdAndSectionId(Long eventId, Long sectionId);
    
    /**
     * Elimina todos los precios de tickets para un evento específico
     */
    void deleteByEventId(Long eventId);
    
    /**
     * Verifica si existen precios de tickets para una sección específica
     */
    @Query("SELECT CASE WHEN COUNT(tp) > 0 THEN true ELSE false END FROM TicketPrice tp WHERE tp.section.id = :sectionId")
    boolean existsBySectionId(@Param("sectionId") Long sectionId);
    
    /**
     * Obtiene estadísticas por sección para un evento específico
     */
    @Query(value = """
        SELECT 
            tp.section_id as sectionId,
            vs.name as sectionName,
            SUM(tp.available_quantity) as totalTicketsForSale,
            COALESCE(COUNT(t.id), 0) as ticketsSold,
            SUM(tp.available_quantity) - COALESCE(COUNT(t.id), 0) as ticketsRemaining,
            ROUND(
                ((SUM(tp.available_quantity) - COALESCE(COUNT(t.id), 0)) * 100.0) / SUM(tp.available_quantity), 
                2
            ) as percentageAvailable
        FROM ticket_prices tp
        LEFT JOIN tickets t ON t.event_id = tp.event_id AND t.section_id = tp.section_id
        JOIN venue_sections vs ON vs.id = tp.section_id
        WHERE tp.event_id = :eventId
        GROUP BY tp.section_id, vs.name
        ORDER BY tp.section_id
    """, nativeQuery = true)
    List<Object[]> getSectionStatisticsByEventId(@Param("eventId") Long eventId);
}
