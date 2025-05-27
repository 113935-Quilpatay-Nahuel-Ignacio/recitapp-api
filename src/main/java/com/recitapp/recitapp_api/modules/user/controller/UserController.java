package com.recitapp.recitapp_api.modules.user.controller;

import com.recitapp.recitapp_api.modules.user.dto.PurchaseHistoryDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserResponseDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserUpdateDTO;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.service.CustomUserDetailsService;
import com.recitapp.recitapp_api.modules.user.service.PurchaseHistoryService;
import com.recitapp.recitapp_api.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Endpoints para gestión de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final PurchaseHistoryService purchaseHistoryService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        UserResponseDTO response = userService.registerUser(registrationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserUpdateDTO updateDTO) {
        UserResponseDTO response = userService.updateUser(id, updateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/purchases")
    public ResponseEntity<List<PurchaseHistoryDTO>> getUserPurchaseHistory(@PathVariable Long userId) {
        List<PurchaseHistoryDTO> purchaseHistory = purchaseHistoryService.getUserPurchaseHistory(userId);
        return ResponseEntity.ok(purchaseHistory);
    }

    @GetMapping("/profile")
    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userDetailsService.getUserByEmail(email);
            
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().getName(),
                    "active", user.getActive(),
                    "registrationDate", user.getRegistrationDate(),
                    "lastConnection", user.getLastConnection()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al obtener el perfil", "message", e.getMessage()));
        }
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Endpoint solo para administradores", description = "Endpoint de prueba que solo pueden acceder los administradores")
    public ResponseEntity<?> adminOnly() {
        return ResponseEntity.ok(Map.of(
                "message", "¡Hola administrador!",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/moderator-or-admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERADOR')")
    @Operation(summary = "Endpoint para moderadores y administradores", description = "Endpoint de prueba para moderadores y administradores")
    public ResponseEntity<?> moderatorOrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(Map.of(
                "message", "¡Hola " + authentication.getName() + "!",
                "authorities", authentication.getAuthorities(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}