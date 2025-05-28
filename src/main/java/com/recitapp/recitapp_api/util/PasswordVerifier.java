package com.recitapp.recitapp_api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para verificar contraseñas y depurar problemas de autenticación
 */
public class PasswordVerifier {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Hashes de la base de datos (de test-users.sql)
        String adminHash = "$2a$10$/j4zhSDyrIozkO6ePYabTOEV4G8JwyKY3Jf.38JK.WNgkA81CQvlm";
        String moderadorHash = "$2a$10$0BG2yEAg7QaSGCrlTpTcNeRwoLvoavsjglAVI.4t4ZNZv7reroynC";
        String usuarioHash = "$2a$10$uv1EOSw7MNtUB.le1uvjK.d9BhuPxdfYwpfOi5XSsxCs4RpI7oKii";
        
        // Contraseñas esperadas
        String adminPassword = "admin123";
        String moderadorPassword = "moderador123";
        String usuarioPassword = "password";
        
        System.out.println("=== VERIFICACIÓN DE CONTRASEÑAS ===");
        System.out.println();
        
        // Verificar admin
        boolean adminMatch = encoder.matches(adminPassword, adminHash);
        System.out.println("Admin (admin@recitapp.com):");
        System.out.println("  Contraseña: " + adminPassword);
        System.out.println("  Hash en BD: " + adminHash);
        System.out.println("  ¿Coincide?: " + (adminMatch ? "✅ SÍ" : "❌ NO"));
        System.out.println();
        
        // Verificar moderador
        boolean moderadorMatch = encoder.matches(moderadorPassword, moderadorHash);
        System.out.println("Moderador (moderador@recitapp.com):");
        System.out.println("  Contraseña: " + moderadorPassword);
        System.out.println("  Hash en BD: " + moderadorHash);
        System.out.println("  ¿Coincide?: " + (moderadorMatch ? "✅ SÍ" : "❌ NO"));
        System.out.println();
        
        // Verificar usuario
        boolean usuarioMatch = encoder.matches(usuarioPassword, usuarioHash);
        System.out.println("Usuario (usuario@recitapp.com):");
        System.out.println("  Contraseña: " + usuarioPassword);
        System.out.println("  Hash en BD: " + usuarioHash);
        System.out.println("  ¿Coincide?: " + (usuarioMatch ? "✅ SÍ" : "❌ NO"));
        System.out.println();
        
        // Generar nuevos hashes para comparar
        System.out.println("=== GENERAR NUEVOS HASHES ===");
        System.out.println("admin123 → " + encoder.encode(adminPassword));
        System.out.println("moderador123 → " + encoder.encode(moderadorPassword));
        System.out.println("password → " + encoder.encode(usuarioPassword));
        System.out.println();
        
        // Verificar si alguna contraseña común funciona con los hashes
        String[] commonPasswords = {"password", "admin", "admin123", "123456", "password123"};
        System.out.println("=== PROBAR CONTRASEÑAS COMUNES ===");
        for (String hash : new String[]{adminHash, moderadorHash, usuarioHash}) {
            System.out.println("Hash: " + hash.substring(0, 20) + "...");
            for (String pwd : commonPasswords) {
                if (encoder.matches(pwd, hash)) {
                    System.out.println("  ✅ Contraseña encontrada: " + pwd);
                }
            }
            System.out.println();
        }
    }
} 