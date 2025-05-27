package com.recitapp.recitapp_api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String refreshToken;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Long expiresIn; // Tiempo de expiración en milisegundos
    private Long refreshExpiresIn; // Tiempo de expiración del refresh token en milisegundos
} 