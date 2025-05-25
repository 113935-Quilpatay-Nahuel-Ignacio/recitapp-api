package com.recitapp.recitapp_api.modules.notification.repository;

import com.recitapp.recitapp_api.modules.notification.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

    Optional<NotificationChannel> findByName(String name);

    boolean existsByName(String name);

    List<NotificationChannel> findByActiveTrue();
}