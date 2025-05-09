package com.recitapp.recitapp_api.modules.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatistics {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @MapsId
    private Event event;

    @Column(name = "total_tickets")
    private Integer totalTickets;

    @Column(name = "sold_tickets")
    private Integer soldTickets;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}