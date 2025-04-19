package com.recitapp.recitapp_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RecitappApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecitappApplication.class, args);
	}

}
