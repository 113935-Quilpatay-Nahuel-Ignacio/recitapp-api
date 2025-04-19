package com.recitapp.recitapp_api.modules.transaction.entity;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {

    @Id
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Id
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Configure composite primary key
    @Embeddable
    public static class TransactionDetailId implements java.io.Serializable {
        @Column(name = "transaction_id")
        private Long transactionId;

        @Column(name = "ticket_id")
        private Long ticketId;

        // equals and hashCode methods
    }

    @EmbeddedId
    private TransactionDetailId id = new TransactionDetailId();
}
