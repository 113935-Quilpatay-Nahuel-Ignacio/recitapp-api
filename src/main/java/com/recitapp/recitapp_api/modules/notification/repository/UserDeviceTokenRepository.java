package com.recitapp.recitapp_api.modules.notification.repository;

import com.recitapp.recitapp_api.modules.notification.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    /**
     * Encuentra todos los tokens activos de un usuario
     */
    List<UserDeviceToken> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Encuentra un token específico de un usuario
     */
    Optional<UserDeviceToken> findByUserIdAndDeviceToken(Long userId, String deviceToken);

    /**
     * Encuentra todos los tokens de un tipo de dispositivo específico
     */
    List<UserDeviceToken> findByDeviceTypeAndIsActiveTrue(UserDeviceToken.DeviceType deviceType);

    /**
     * Verifica si existe un token específico
     */
    boolean existsByDeviceToken(String deviceToken);

    /**
     * Desactiva todos los tokens de un usuario
     */
    @Modifying
    @Query("UPDATE UserDeviceToken udt SET udt.isActive = false WHERE udt.user.id = :userId")
    void deactivateAllTokensForUser(@Param("userId") Long userId);

    /**
     * Desactiva un token específico
     */
    @Modifying
    @Query("UPDATE UserDeviceToken udt SET udt.isActive = false WHERE udt.deviceToken = :deviceToken")
    void deactivateToken(@Param("deviceToken") String deviceToken);

    /**
     * Encuentra el token más reciente de un usuario para un tipo de dispositivo
     */
    @Query("SELECT udt FROM UserDeviceToken udt WHERE udt.user.id = :userId " +
           "AND udt.deviceType = :deviceType AND udt.isActive = true " +
           "ORDER BY udt.updatedAt DESC")
    Optional<UserDeviceToken> findLatestTokenByUserAndDeviceType(@Param("userId") Long userId, 
                                                                @Param("deviceType") UserDeviceToken.DeviceType deviceType);

    /**
     * Elimina tokens inactivos antiguos (limpieza)
     */
    @Modifying
    @Query("DELETE FROM UserDeviceToken udt WHERE udt.isActive = false " +
           "AND udt.updatedAt < :cutoffDate")
    void deleteInactiveTokensOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);

    /**
     * Cuenta tokens activos por usuario
     */
    @Query("SELECT COUNT(udt) FROM UserDeviceToken udt WHERE udt.user.id = :userId AND udt.isActive = true")
    Long countActiveTokensByUser(@Param("userId") Long userId);
} 