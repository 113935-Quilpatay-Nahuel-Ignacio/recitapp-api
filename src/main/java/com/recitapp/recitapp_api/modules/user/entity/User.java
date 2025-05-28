package com.recitapp.recitapp_api.modules.user.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column
    private String password;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(length = 20, unique = true)
    private String dni;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "last_connection")
    private LocalDateTime lastConnection;

    @Column(nullable = false)
    private Boolean active;

    @Column(length = 50)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "auth_method", length = 20)
    private String authMethod;

    @Column(name = "wallet_balance")
    private Double walletBalance;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    // ================================================================================
    // RELACIONES BIDIRECCIONALES CON ELIMINACIÓN EN CASCADA
    // ================================================================================

    // Tokens de refresh - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RefreshToken> refreshTokens;

    // Tokens de reset de contraseña - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PasswordResetToken> passwordResetTokens;

    // Preferencias de notificación - eliminación en cascada
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private com.recitapp.recitapp_api.modules.notification.entity.NotificationPreference notificationPreference;

    // Historial de notificaciones - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.notification.entity.NotificationHistory> notificationHistory;

    // Transacciones - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.transaction.entity.Transaction> transactions;

    // Tickets comprados - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.ticket.entity.Ticket> tickets;

    // Eventos guardados - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.event.entity.SavedEvent> savedEvents;

    // Artistas seguidos - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.artist.entity.ArtistFollower> artistFollowers;

    // Venues seguidos - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.venue.entity.VenueFollower> venueFollowers;

    // Sala de espera - eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<com.recitapp.recitapp_api.modules.event.entity.WaitingRoom> waitingRoomEntries;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
        if (walletBalance == null) {
            walletBalance = 0.0;
        }
    }
}