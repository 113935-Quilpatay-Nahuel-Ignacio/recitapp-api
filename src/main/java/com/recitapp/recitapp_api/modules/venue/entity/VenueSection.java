package com.recitapp.recitapp_api.modules.venue.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venue_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active;

    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
    }
}
