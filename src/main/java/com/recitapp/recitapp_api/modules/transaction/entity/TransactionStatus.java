package com.recitapp.recitapp_api.modules.transaction.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
