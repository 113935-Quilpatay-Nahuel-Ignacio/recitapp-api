package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.modules.user.dto.AdminUserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserResponseDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserUpdateDTO;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResponseDTO registerUser(UserRegistrationDTO registrationDTO);
    UserResponseDTO createUserWithRole(AdminUserRegistrationDTO registrationDTO);
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO);
    void deleteUser(Long id);
    Map<String, Object> getUserRelatedDataSummary(Long id);
}