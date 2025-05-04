package com.recitapp.recitapp_api.modules.user.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
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

    @Column(name = "auth_method", length = 20)
    private String authMethod;

    @Column(name = "wallet_balance", precision = 10, scale = 2)
    private BigDecimal walletBalance;

    @Column(name = "firebase_uid", unique = true)
    private String firebaseUid;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
        if (walletBalance == null) {
            walletBalance = BigDecimal.ZERO;
        }
    }
}