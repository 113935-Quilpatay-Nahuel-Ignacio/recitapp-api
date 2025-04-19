package com.recitapp.recitapp_api.modules.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Boolean active;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
    }
}
