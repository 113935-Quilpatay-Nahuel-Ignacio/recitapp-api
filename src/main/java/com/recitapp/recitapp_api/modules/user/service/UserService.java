package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.modules.user.dto.AdminUserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO registrationDTO);
    UserResponseDTO createUserWithRole(AdminUserRegistrationDTO registrationDTO);
}