package com.recitapp.recitapp_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Recitapp API")
                        .description("API for concert tickets platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Recitapp Team")
                                .email("contact@recitapp.com")
                                .url("https://recitapp.com")));
    }
}
