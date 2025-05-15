package com.recitapp.recitapp_api.modules.ticket.repository;

import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketStatusRepository extends JpaRepository<TicketStatus, Long> {

    // Find by status name
    Optional<TicketStatus> findByName(String name);
}