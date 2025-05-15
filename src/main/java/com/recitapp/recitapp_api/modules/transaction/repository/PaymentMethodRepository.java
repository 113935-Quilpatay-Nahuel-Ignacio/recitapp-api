package com.recitapp.recitapp_api.modules.transaction.repository;

import com.recitapp.recitapp_api.modules.transaction.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // Find active payment methods
    List<PaymentMethod> findByActiveTrue();

    // Find by name
    Optional<PaymentMethod> findByName(String name);

    // Check if exists by name
    boolean existsByName(String name);
}