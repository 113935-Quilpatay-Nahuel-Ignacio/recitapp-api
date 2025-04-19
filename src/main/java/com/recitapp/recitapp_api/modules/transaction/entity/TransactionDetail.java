package com.recitapp.recitapp_api.modules.transaction.entity;

import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "transaction_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {

    @EmbeddedId
    private TransactionDetailId id;

    @MapsId("transactionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @MapsId("ticketId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionDetailId implements Serializable {
        @Column(name = "transaction_id")
        private Long transactionId;

        @Column(name = "ticket_id")
        private Long ticketId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionDetailId that = (TransactionDetailId) o;
            return Objects.equals(transactionId, that.transactionId) &&
                    Objects.equals(ticketId, that.ticketId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, ticketId);
        }
    }
}
