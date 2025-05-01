package com.recitapp.recitapp_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

//@Configuration
public class FirebaseConfig {

    /*@Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        ClassPathResource resource = new ClassPathResource(firebaseConfigPath);

        if (!resource.exists()) {
            throw new RuntimeException("El archivo de configuración de Firebase no existe en: " + firebaseConfigPath);
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            return FirebaseAuth.getInstance();
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            throw e;
        }
    }*/
}