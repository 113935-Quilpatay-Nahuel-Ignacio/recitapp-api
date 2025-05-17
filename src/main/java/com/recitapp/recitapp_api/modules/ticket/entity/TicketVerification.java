package com.recitapp.recitapp_api.modules.ticket.entity;

import com.recitapp.recitapp_api.modules.event.entity.AccessPoint;
import com.recitapp.recitapp_api.modules.event.entity.Event;
import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for logging ticket verifications at access points
 */
@Entity
@Table(name = "ticket_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "access_point_id", nullable = false)
    private AccessPoint accessPoint;

    @ManyToOne
    @JoinColumn(name = "verifier_id", nullable = false)
    private User verifier;

    @Column(name = "verification_time", nullable = false)
    private LocalDateTime verificationTime;

    @Column(nullable = false)
    private Boolean successful;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "qr_code_used", length = 500)
    private String qrCodeUsed;

    @PrePersist
    protected void onCreate() {
        if (verificationTime == null) {
            verificationTime = LocalDateTime.now();
        }
    }
}