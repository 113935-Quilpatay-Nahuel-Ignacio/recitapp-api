package com.recitapp.recitapp_api.modules.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "La contraseña debe contener al menos una letra minuscula, una mayuscula y un numero")
    private String password;

    @NotBlank(message = "La confirmacion de contraseña es obligatoria")
    private String confirmPassword;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{7,9}$", message = "El DNI debe tener entre 7 y 9 digitos")
    private String dni;

    @Pattern(regexp = "^\\+[1-9][0-9]{9,14}$", message = "El telefono debe comenzar con '+' seguido del codigo de pais y numero (ej: +541112345678)")
    private String phone;

    private String address;
}
