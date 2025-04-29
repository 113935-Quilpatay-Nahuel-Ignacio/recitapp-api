package com.recitapp.recitapp_api.modules.user.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.user.dto.AdminUserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserResponseDTO;
import com.recitapp.recitapp_api.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
//@RequireRole("ADMIN")
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserResponseDTO> createUserWithRole(@Valid @RequestBody AdminUserRegistrationDTO registrationDTO) {
        UserResponseDTO response = userService.createUserWithRole(registrationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}