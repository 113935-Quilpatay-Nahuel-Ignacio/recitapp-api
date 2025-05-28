package com.recitapp.recitapp_api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckSpecificHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Hash específico del usuario
        String targetHash = "$2a$10$RBnWiXe9fWXZ7bAOhX8iZepGpBBk2C9fDAV75hWLc5MFRRdnKGJPi";
        
        // Lista de contraseñas comunes para probar
        String[] passwords = {
            "password", "admin", "admin123", "123456", "password123",
            "test", "test123", "user", "usuario", "moderador123", 
            "comprador123", "recitapp", "recit123", "app123", 
            "demo", "demo123", "qwerty", "abc123", "letmein", 
            "welcome", "hello", "guest", "guest123", "super123",
            "mod123", "manager123", "registrador123", "12345",
            "1234", "asdf", "zxcv", "root", "toor", "pass"
        };
        
        System.out.println("Verificando hash: " + targetHash);
        System.out.println("Probando contraseñas comunes...\n");
        
        boolean found = false;
        for (String password : passwords) {
            if (encoder.matches(password, targetHash)) {
                System.out.println("¡ENCONTRADA!");
                System.out.println("La contraseña es: " + password);
                found = true;
                break;
            }
        }
        
        if (!found) {
            System.out.println("No se encontró la contraseña entre las opciones comunes.");
            System.out.println("Puede ser una contraseña personalizada o más compleja.");
        }
    }
} 