package com.recitapp.recitapp_api.modules.notification.repository;

import com.recitapp.recitapp_api.modules.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {

    Optional<NotificationType> findByName(String name);

    boolean existsByName(String name);
}