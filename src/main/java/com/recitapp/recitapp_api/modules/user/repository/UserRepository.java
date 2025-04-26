package com.recitapp.recitapp_api.modules.user.repository;

import com.recitapp.recitapp_api.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    Optional<User> findByEmail(String email);
    Optional<User> findByFirebaseUid(String firebaseUid);
}