package com.recitapp.recitapp_api.modules.event.repository;

import com.recitapp.recitapp_api.modules.event.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventStatusRepository extends JpaRepository<EventStatus, Long> {

    // Encontrar por nombre
    Optional<EventStatus> findByName(String name);

    // Verificar si existe por nombre
    boolean existsByName(String name);
}