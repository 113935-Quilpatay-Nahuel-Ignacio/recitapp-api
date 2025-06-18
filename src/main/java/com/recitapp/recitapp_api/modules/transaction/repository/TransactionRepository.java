package com.recitapp.recitapp_api.modules.transaction.repository;

import com.recitapp.recitapp_api.modules.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByPaymentMethodIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long paymentMethodId, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByStatusNameAndTransactionDateBetweenOrderByTransactionDateDesc(
            String statusName, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.paymentMethod.id = :paymentMethodId")
    long countByPaymentMethodId(@Param("paymentMethodId") Long paymentMethodId);

    // MercadoPago webhook integration
    Optional<Transaction> findByExternalReference(String externalReference);
}