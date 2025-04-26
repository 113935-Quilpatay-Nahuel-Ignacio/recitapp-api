package com.recitapp.recitapp_api.modules.user.service.impl;

import com.google.firebase.auth.UserRecord;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.user.dto.AdminUserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserRegistrationDTO;
import com.recitapp.recitapp_api.modules.user.dto.UserResponseDTO;
import com.recitapp.recitapp_api.modules.user.entity.Role;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.RoleRepository;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.service.UserService;
import com.recitapp.recitapp_api.service.FirebaseUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FirebaseUserService firebaseUserService;

    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RecitappException("El email ya está registrado");
        }

        if (userRepository.existsByDni(registrationDTO.getDni())) {
            throw new RecitappException("El DNI ya está registrado");
        }

        Role buyerRole = roleRepository.findByName("COMPRADOR")
                .orElseThrow(() -> new RecitappException("Rol de comprador no encontrado"));

        try {
            UserRecord firebaseUser;

            if (registrationDTO.getFirebaseUid() != null && !registrationDTO.getFirebaseUid().isEmpty()) {
                firebaseUserService.updateUserRole(registrationDTO.getFirebaseUid(), "COMPRADOR");
                firebaseUser = firebaseUserService.getUser(registrationDTO.getFirebaseUid());
            } else {
                firebaseUser = firebaseUserService.createUserWithRole(
                        registrationDTO.getEmail(),
                        registrationDTO.getPassword(),
                        "COMPRADOR"
                );
            }

            User user = getUser(registrationDTO, buyerRole, firebaseUser);

            User savedUser = userRepository.save(user);
            return mapToResponseDTO(savedUser);

        } catch (Exception e) {
            throw new RecitappException("Error al crear usuario: " + e.getMessage());
        }
    }

    private static User getUser(UserRegistrationDTO registrationDTO, Role buyerRole, UserRecord firebaseUser) {
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setDni(registrationDTO.getDni());
        user.setCountry(registrationDTO.getCountry());
        user.setCity(registrationDTO.getCity());
        user.setRole(buyerRole);
        user.setAuthMethod(registrationDTO.getFirebaseUid() != null ? "FIREBASE" : "EMAIL");
        user.setFirebaseUid(firebaseUser.getUid());
        return user;
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
            UserRecord firebaseUser = firebaseUserService.createUserWithRole(
                    registrationDTO.getEmail(),
                    registrationDTO.getPassword(),
                    registrationDTO.getRoleName()
            );

            User user = new User();
            user.setEmail(registrationDTO.getEmail());
            user.setFirstName(registrationDTO.getFirstName());
            user.setLastName(registrationDTO.getLastName());
            user.setDni(registrationDTO.getDni());
            user.setCountry(registrationDTO.getCountry());
            user.setCity(registrationDTO.getCity());
            user.setRole(role);
            user.setAuthMethod("EMAIL");
            user.setFirebaseUid(firebaseUser.getUid());

            User savedUser = userRepository.save(user);
            return mapToResponseDTO(savedUser);

        } catch (Exception e) {
            throw new RecitappException("Error al crear usuario: " + e.getMessage());
        }
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