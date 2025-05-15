package com.recitapp.recitapp_api.modules.transaction.repository;

import com.recitapp.recitapp_api.modules.transaction.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, TransactionDetail.TransactionDetailId> {

    // Find by transaction ID
    List<TransactionDetail> findByTransactionId(Long transactionId);

    // Find by ticket ID
    List<TransactionDetail> findByTicketId(Long ticketId);

    // Delete by transaction ID
    void deleteByTransactionId(Long transactionId);

    // Count by transaction ID
    @Query("SELECT COUNT(td) FROM TransactionDetail td WHERE td.transaction.id = :transactionId")
    Long countByTransactionId(@Param("transactionId") Long transactionId);

    // Calculate total amount for a transaction
    @Query("SELECT SUM(td.unitPrice) FROM TransactionDetail td WHERE td.transaction.id = :transactionId")
    java.math.BigDecimal calculateTotalAmountByTransactionId(@Param("transactionId") Long transactionId);
}