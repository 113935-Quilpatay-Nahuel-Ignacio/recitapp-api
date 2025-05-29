package com.recitapp.recitapp_api.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String dni;
    private String country;
    private String city;
    private LocalDateTime registrationDate;
    private String roleName;
    private String authMethod;
    private Boolean active;
}