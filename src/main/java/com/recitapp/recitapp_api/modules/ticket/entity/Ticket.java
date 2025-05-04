package com.recitapp.recitapp_api.modules.ticket.entity;

import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.event.entity.Promotion;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.venue.entity.VenueSection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private VenueSection section;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private TicketStatus status;

    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "identification_code", length = 50, unique = true, nullable = false)
    private String identificationCode;

    @Column(name = "qr_code", unique = true, nullable = false, length = 500)
    private String qrCode;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "assigned_user_first_name", length = 100, nullable = false)
    private String assignedUserFirstName;

    @Column(name = "assigned_user_last_name", length = 100, nullable = false)
    private String assignedUserLastName;

    @Column(name = "assigned_user_dni", length = 20, nullable = false)
    private String assignedUserDni;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "use_date")
    private LocalDateTime useDate;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Column(name = "is_gift")
    private Boolean isGift;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        purchaseDate = LocalDateTime.now();
        if (isGift == null) {
            isGift = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
