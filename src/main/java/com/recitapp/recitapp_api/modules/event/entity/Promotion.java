package com.recitapp.recitapp_api.modules.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "minimum_quantity", nullable = false)
    private Integer minimumQuantity;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "apply_to_total")
    private Boolean applyToTotal;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "promotion_code", length = 50, unique = true)
    private String promotionCode;

    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
        if (applyToTotal == null) {
            applyToTotal = false;
        }
    }
}
