package com.recitapp.recitapp_api.modules.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String name;

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
