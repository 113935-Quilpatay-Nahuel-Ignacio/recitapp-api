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
}
