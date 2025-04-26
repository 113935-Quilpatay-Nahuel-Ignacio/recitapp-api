package com.recitapp.recitapp_api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserRegistrationDTO extends UserRegistrationDTO {

    @NotBlank(message = "El rol es obligatorio")
    private String roleName;
}