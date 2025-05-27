package com.recitapp.recitapp_api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar contraseñas encriptadas con BCrypt
 * Ejecutar este main para generar contraseñas para usuarios de prueba
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generar contraseñas encriptadas
        String password = "password";
        String admin123 = "admin123";
        String moderador123 = "moderador123";
        String usuario123 = "usuario123";
        
        System.out.println("Contraseñas encriptadas:");
        System.out.println("password: " + encoder.encode(password));
        System.out.println("admin123: " + encoder.encode(admin123));
        System.out.println("moderador123: " + encoder.encode(moderador123));
        System.out.println("usuario123: " + encoder.encode(usuario123));
        
        // Verificar que las contraseñas coinciden
        String encodedPassword = encoder.encode(password);
        System.out.println("\nVerificación:");
        System.out.println("¿'password' coincide? " + encoder.matches(password, encodedPassword));
    }
} 