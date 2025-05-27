package com.recitapp.recitapp_api.modules.user.repository;

import com.recitapp.recitapp_api.modules.user.entity.PasswordResetToken;
import com.recitapp.recitapp_api.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true, prt.usedAt = :usedAt WHERE prt.user = :user AND prt.used = false")
    void invalidateAllUserTokens(User user, LocalDateTime usedAt);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now")
    void deleteExpiredTokens(LocalDateTime now);

    @Query("SELECT COUNT(prt) FROM PasswordResetToken prt WHERE prt.user = :user AND prt.createdAt > :since")
    long countRecentTokensByUser(User user, LocalDateTime since);
} 