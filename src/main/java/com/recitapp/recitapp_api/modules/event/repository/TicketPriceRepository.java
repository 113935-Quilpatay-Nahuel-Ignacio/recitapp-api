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
}
