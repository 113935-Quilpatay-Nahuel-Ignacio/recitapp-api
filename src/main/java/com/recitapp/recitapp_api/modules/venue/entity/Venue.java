package com.recitapp.recitapp_api.modules.venue.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "venues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "google_maps_url", length = 500)
    private String googleMapsUrl;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "web_url", length = 500)
    private String webUrl;

    @Column(length = 500)
    private String image;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VenueSection> sections;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}