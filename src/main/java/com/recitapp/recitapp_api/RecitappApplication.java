package com.recitapp.recitapp_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RecitappApplication {

	public static void main(String[] args) {
		System.out.println("=== STARTING RECITAPP APPLICATION ===");
		System.out.println("🔍 Using default component scanning from package: com.recitapp.recitapp_api");
		
		SpringApplication.run(RecitappApplication.class, args);
	}

}
