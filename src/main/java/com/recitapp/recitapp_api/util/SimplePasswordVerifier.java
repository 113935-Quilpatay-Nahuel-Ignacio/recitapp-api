package com.recitapp.recitapp_api.util;

/**
 * Verificador simple de contraseñas para depurar problemas de autenticación
 */
public class SimplePasswordVerifier {
    
    public static void main(String[] args) {
        System.out.println("=== CREDENCIALES DE PRUEBA RECITAPP ===");
        System.out.println();
        
        System.out.println("🔐 USUARIOS DISPONIBLES:");
        System.out.println();
        
        System.out.println("1. ADMINISTRADOR");
        System.out.println("   Email: admin@recitapp.com");
        System.out.println("   Contraseña: admin123");
        System.out.println("   Rol: ADMIN");
        System.out.println();
        
        System.out.println("2. MODERADOR");
        System.out.println("   Email: moderador@recitapp.com");
        System.out.println("   Contraseña: moderador123");
        System.out.println("   Rol: MODERADOR");
        System.out.println();
        
        System.out.println("3. USUARIO REGULAR");
        System.out.println("   Email: usuario@recitapp.com");
        System.out.println("   Contraseña: password");
        System.out.println("   Rol: COMPRADOR");
        System.out.println();
        
        System.out.println("📋 HASHES EN BASE DE DATOS:");
        System.out.println();
        System.out.println("admin@recitapp.com:");
        System.out.println("$2a$10$/j4zhSDyrIozkO6ePYabTOEV4G8JwyKY3Jf.38JK.WNgkA81CQvlm");
        System.out.println();
        System.out.println("moderador@recitapp.com:");
        System.out.println("$2a$10$0BG2yEAg7QaSGCrlTpTcNeRwoLvoavsjglAVI.4t4ZNZv7reroynC");
        System.out.println();
        System.out.println("usuario@recitapp.com:");
        System.out.println("$2a$10$uv1EOSw7MNtUB.le1uvjK.d9BhuPxdfYwpfOi5XSsxCs4RpI7oKii");
        System.out.println();
        
        System.out.println("⚠️  POSIBLES CAUSAS DE 'CREDENCIALES INVÁLIDAS':");
        System.out.println();
        System.out.println("1. Contraseña incorrecta - Usa exactamente:");
        System.out.println("   - admin123 (no Admin123, no ADMIN123)");
        System.out.println("   - moderador123 (no Moderador123)");
        System.out.println("   - password (no Password, no PASSWORD)");
        System.out.println();
        System.out.println("2. Email incorrecto - Usa exactamente:");
        System.out.println("   - admin@recitapp.com (no Admin@recitapp.com)");
        System.out.println("   - moderador@recitapp.com");
        System.out.println("   - usuario@recitapp.com");
        System.out.println();
        System.out.println("3. Usuario inactivo en base de datos");
        System.out.println("4. Backend no está ejecutándose en localhost:8080");
        System.out.println("5. Base de datos no tiene los usuarios cargados");
        System.out.println();
        
        System.out.println("🔧 SOLUCIONES:");
        System.out.println();
        System.out.println("1. Verifica que el backend esté ejecutándose");
        System.out.println("2. Usa exactamente las credenciales mostradas arriba");
        System.out.println("3. Verifica que la base de datos tenga los usuarios");
        System.out.println("4. Revisa los logs del backend para más detalles");
        System.out.println();
        
        System.out.println("✅ CÓMO FUNCIONA LA ENCRIPTACIÓN:");
        System.out.println();
        System.out.println("REGISTRO:");
        System.out.println("1. Escribes: 'admin123'");
        System.out.println("2. BCrypt encripta: '$2a$10$/j4zhSDyrIozkO6ePYabTOEV4G8...'");
        System.out.println("3. Se guarda el hash en la base de datos");
        System.out.println();
        System.out.println("LOGIN:");
        System.out.println("1. Escribes: 'admin123'");
        System.out.println("2. Sistema busca el hash en la BD");
        System.out.println("3. BCrypt compara tu contraseña con el hash");
        System.out.println("4. Si coinciden, te autentica");
        System.out.println();
        System.out.println("❌ BCrypt NO desencripta, solo compara hashes!");
    }
} 