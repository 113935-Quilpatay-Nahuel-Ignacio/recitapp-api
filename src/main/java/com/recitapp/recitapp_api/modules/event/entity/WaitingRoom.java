package com.recitapp.recitapp_api.modules.event.entity;

import com.recitapp.recitapp_api.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_room")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date")
    private LocalDateTime entryDate;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "access_token", unique = true)
    private String accessToken;

    @Column(name = "access_date")
    private LocalDateTime accessDate;

    @Column(name = "maximum_time_minutes")
    private Integer maximumTimeMinutes;

    @Column(nullable = false)
    private Boolean completed;

    @PrePersist
    protected void onCreate() {
        entryDate = LocalDateTime.now();
        if (completed == null) {
            completed = false;
        }
        if (maximumTimeMinutes == null) {
            maximumTimeMinutes = 10; // Default 10 minutes
        }
    }
}
