package com.recitapp.recitapp_api.modules.user.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.user.dto.AdminUserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserResponseDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserUpdateDTO;
import com.recitapp.recitapp_api.modules.user.entity.Role;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.RoleRepository;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RecitappException("El email ya está registrado");
        }

        if (userRepository.existsByDni(registrationDTO.getDni())) {
            throw new RecitappException("El DNI ya está registrado");
        }

        // Determinar el rol basado en el dominio del email
        Role userRole;
        String email = registrationDTO.getEmail().toLowerCase(); // Normalizar a minúsculas
        
        if (email.endsWith("@recitapp-admin.com")) {
            userRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RecitappException("Rol ADMIN no encontrado"));
            log.info("🔐 ASIGNACIÓN AUTOMÁTICA: Rol ADMIN asignado al email: {}", registrationDTO.getEmail());
        } else if (email.endsWith("@recitapp-moderator.com")) {
            userRole = roleRepository.findByName("MODERADOR")
                    .orElseThrow(() -> new RecitappException("Rol MODERADOR no encontrado"));
            log.info("🛡️ ASIGNACIÓN AUTOMÁTICA: Rol MODERADOR asignado al email: {}", registrationDTO.getEmail());
        } else if (email.endsWith("@recitapp-verifier.com")) {
            userRole = roleRepository.findByName("REGISTRADOR_EVENTO")
                    .orElseThrow(() -> new RecitappException("Rol REGISTRADOR_EVENTO no encontrado"));
            log.info("📝 ASIGNACIÓN AUTOMÁTICA: Rol REGISTRADOR_EVENTO asignado al email: {}", registrationDTO.getEmail());
        } else {
            userRole = roleRepository.findByName("COMPRADOR")
                    .orElseThrow(() -> new RecitappException("Rol COMPRADOR no encontrado"));
            log.info("👤 Rol por defecto COMPRADOR asignado al email: {}", registrationDTO.getEmail());
        }

        try {
            User user = new User();
            user.setEmail(registrationDTO.getEmail());
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setDni(registrationDTO.getDni());
            user.setCountry(registrationDTO.getCountry());
            user.setCity(registrationDTO.getCity());
            user.setRole(userRole);
            user.setAuthMethod("EMAIL");
            user.setPassword(registrationDTO.getPassword());

            user.setFirebaseUid("temp-" + UUID.randomUUID());

            User savedUser = userRepository.save(user);
            log.info("✅ Usuario registrado exitosamente: {} con rol: {} (ID: {})", 
                    savedUser.getEmail(), savedUser.getRole().getName(), savedUser.getRole().getId());
            return mapToResponseDTO(savedUser);
        } catch (Exception e) {
            log.error("❌ Error al crear usuario {}: {}", registrationDTO.getEmail(), e.getMessage());
            throw new RecitappException("Error al crear usuario: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserResponseDTO createUserWithRole(AdminUserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RecitappException("El email ya está registrado");
        }

        if (userRepository.existsByDni(registrationDTO.getDni())) {
            throw new RecitappException("El DNI ya está registrado");
        }

        Role role = roleRepository.findByName(registrationDTO.getRoleName())
                .orElseThrow(() -> new RecitappException("Rol no encontrado: " + registrationDTO.getRoleName()));

        try {
            User user = new User();
            user.setEmail(registrationDTO.getEmail());
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setDni(registrationDTO.getDni());
            user.setCountry(registrationDTO.getCountry());
            user.setCity(registrationDTO.getCity());
            user.setRole(role);
            user.setAuthMethod("EMAIL");
            user.setPassword(registrationDTO.getPassword());
            user.setFirebaseUid("temp-" + UUID.randomUUID());

            User savedUser = userRepository.save(user);
            return mapToResponseDTO(savedUser);

        } catch (Exception e) {
            throw new RecitappException("Error al crear usuario: " + e.getMessage());
        }
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + id));
        return mapToResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + id));

        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDTO.getEmail())) {
                throw new RecitappException("El email ya está registrado");
            }
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }

        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }

        if (updateDTO.getCountry() != null) {
            user.setCountry(updateDTO.getCountry());
        }

        if (updateDTO.getCity() != null) {
            user.setCity(updateDTO.getCity());
        }

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(updateDTO.getPassword());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Iniciando eliminación del usuario con ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + id));

        try {
            // Log información del usuario antes de eliminar
            log.info("Eliminando usuario: {} {} ({})", user.getFirstName(), user.getLastName(), user.getEmail());
            log.info("Rol del usuario: {}", user.getRole().getName());
            
            // Verificar si el usuario tiene datos relacionados (opcional, para logging)
            logRelatedDataInfo(user);
            
            // La eliminación en cascada se manejará automáticamente por JPA y las constraints de BD
            userRepository.deleteById(id);
            
            log.info("Usuario con ID {} eliminado exitosamente junto con todos sus datos relacionados", id);
            
        } catch (Exception e) {
            log.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            throw new RecitappException("Error al eliminar usuario: " + e.getMessage());
        }
    }

    /**
     * Registra información sobre los datos relacionados del usuario antes de la eliminación
     * Esto es útil para auditoría y debugging
     */
    private void logRelatedDataInfo(User user) {
        try {
            log.info("Datos relacionados que serán eliminados en cascada para usuario ID {}:", user.getId());
            
            if (user.getRefreshTokens() != null) {
                log.info("- {} refresh tokens", user.getRefreshTokens().size());
            }
            
            if (user.getPasswordResetTokens() != null) {
                log.info("- {} password reset tokens", user.getPasswordResetTokens().size());
            }
            
            if (user.getNotificationHistory() != null) {
                log.info("- {} notificaciones en historial", user.getNotificationHistory().size());
            }
            
            if (user.getTransactions() != null) {
                log.info("- {} transacciones", user.getTransactions().size());
            }
            
            if (user.getTickets() != null) {
                log.info("- {} tickets", user.getTickets().size());
            }
            
            if (user.getSavedEvents() != null) {
                log.info("- {} eventos guardados", user.getSavedEvents().size());
            }
            
            if (user.getArtistFollowers() != null) {
                log.info("- {} artistas seguidos", user.getArtistFollowers().size());
            }
            
            if (user.getVenueFollowers() != null) {
                log.info("- {} venues seguidos", user.getVenueFollowers().size());
            }
            
            if (user.getWaitingRoomEntries() != null) {
                log.info("- {} entradas en sala de espera", user.getWaitingRoomEntries().size());
            }
            
            if (user.getNotificationPreference() != null) {
                log.info("- 1 configuración de preferencias de notificación");
            }
            
        } catch (Exception e) {
            // Si hay error al obtener datos relacionados, no fallar la eliminación
            log.warn("No se pudo obtener información completa de datos relacionados: {}", e.getMessage());
        }
    }

    /**
     * Obtiene un resumen de todos los datos relacionados de un usuario
     * Útil para que los administradores vean el impacto antes de eliminar
     */
    @Override
    public Map<String, Object> getUserRelatedDataSummary(Long id) {
        log.info("Obteniendo resumen de datos relacionados para usuario ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RecitappException("Usuario no encontrado con ID: " + id));

        Map<String, Object> summary = new HashMap<>();
        
        // Información básica del usuario
        summary.put("userId", user.getId());
        summary.put("email", user.getEmail());
        summary.put("fullName", user.getFirstName() + " " + user.getLastName());
        summary.put("role", user.getRole().getName());
        summary.put("registrationDate", user.getRegistrationDate());
        summary.put("active", user.getActive());
        
        // Contadores de datos relacionados
        Map<String, Integer> relatedDataCounts = new HashMap<>();
        
        try {
            relatedDataCounts.put("refreshTokens", user.getRefreshTokens() != null ? user.getRefreshTokens().size() : 0);
            relatedDataCounts.put("passwordResetTokens", user.getPasswordResetTokens() != null ? user.getPasswordResetTokens().size() : 0);
            relatedDataCounts.put("notificationHistory", user.getNotificationHistory() != null ? user.getNotificationHistory().size() : 0);
            relatedDataCounts.put("transactions", user.getTransactions() != null ? user.getTransactions().size() : 0);
            relatedDataCounts.put("tickets", user.getTickets() != null ? user.getTickets().size() : 0);
            relatedDataCounts.put("savedEvents", user.getSavedEvents() != null ? user.getSavedEvents().size() : 0);
            relatedDataCounts.put("artistFollowers", user.getArtistFollowers() != null ? user.getArtistFollowers().size() : 0);
            relatedDataCounts.put("venueFollowers", user.getVenueFollowers() != null ? user.getVenueFollowers().size() : 0);
            relatedDataCounts.put("waitingRoomEntries", user.getWaitingRoomEntries() != null ? user.getWaitingRoomEntries().size() : 0);
            relatedDataCounts.put("notificationPreferences", user.getNotificationPreference() != null ? 1 : 0);
            
        } catch (Exception e) {
            log.warn("Error al obtener algunos contadores de datos relacionados: {}", e.getMessage());
            relatedDataCounts.put("error", -1);
        }
        
        summary.put("relatedDataCounts", relatedDataCounts);
        
        // Calcular total de registros que serán eliminados
        int totalRelatedRecords = relatedDataCounts.values().stream()
                .filter(count -> count > 0)
                .mapToInt(Integer::intValue)
                .sum();
        
        summary.put("totalRelatedRecords", totalRelatedRecords);
        
        // Advertencias y recomendaciones
        List<String> warnings = new ArrayList<>();
        
        if (totalRelatedRecords > 0) {
            warnings.add("Este usuario tiene " + totalRelatedRecords + " registros relacionados que serán eliminados permanentemente");
        }
        
        if (relatedDataCounts.get("tickets") > 0) {
            warnings.add("El usuario tiene tickets comprados. La eliminación afectará el historial de ventas");
        }
        
        if (relatedDataCounts.get("transactions") > 0) {
            warnings.add("El usuario tiene transacciones registradas. Se perderá el historial financiero");
        }
        
        if ("ADMIN".equals(user.getRole().getName()) || "MODERADOR".equals(user.getRole().getName())) {
            warnings.add("¡ATENCIÓN! Este es un usuario con rol privilegiado (" + user.getRole().getName() + ")");
        }
        
        summary.put("warnings", warnings);
        summary.put("deletionImpact", totalRelatedRecords > 10 ? "ALTO" : totalRelatedRecords > 5 ? "MEDIO" : "BAJO");
        
        return summary;
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dni(user.getDni())
                .country(user.getCountry())
                .city(user.getCity())
                .registrationDate(user.getRegistrationDate())
                .roleName(user.getRole().getName())
                .authMethod(user.getAuthMethod())
                .build();
    }
}