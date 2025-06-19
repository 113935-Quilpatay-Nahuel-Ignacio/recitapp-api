package com.recitapp.recitapp_api.modules.event.entity;

import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private VenueSection section;

    @Column(name = "ticket_type", length = 50, nullable = false)
    private String ticketType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    // Nuevos campos para entradas promocionales y de regalo
    @Column(name = "is_promotional", nullable = false)
    private Boolean isPromotional = false;
    
    @Column(name = "is_gift", nullable = false)
    private Boolean isGift = false;
    
    @Column(name = "promotional_type", length = 20)
    private String promotionalType; // "2X1", "GIFT", etc.
    
    @Column(name = "seats_per_ticket", nullable = false)
    private Integer seatsPerTicket = 1; // Para 2x1 sería 2, para regalo 1
}
