package com.recitapp.recitapp_api.modules.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_history_id", nullable = false)
    private NotificationHistory notificationHistory;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus;

    @Column(name = "delivery_timestamp")
    private LocalDateTime deliveryTimestamp;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "provider_response", columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        if (deliveryTimestamp == null) {
            deliveryTimestamp = LocalDateTime.now();
        }
    }

    public enum DeliveryStatus {
        SENT,       // Enviado desde nuestro sistema
        DELIVERED,  // Confirmado como entregado por el proveedor
        FAILED,     // Falló el envío
        OPENED,     // Email abierto o push notification vista
        CLICKED     // Link clickeado o acción tomada
    }
} 