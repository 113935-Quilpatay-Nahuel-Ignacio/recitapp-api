package com.recitapp.recitapp_api.modules.transaction.repository;

import com.recitapp.recitapp_api.modules.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, Long> {

    // Find by status name
    Optional<TransactionStatus> findByName(String name);
}