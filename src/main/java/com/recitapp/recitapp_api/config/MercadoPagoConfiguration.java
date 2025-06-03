package com.recitapp.recitapp_api.config;

import com.mercadopago.MercadoPagoConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.public.key}")
    private String publicKey;

    @PostConstruct
    public void initMercadoPago() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("MercadoPago SDK configured successfully");
        } catch (Exception e) {
            log.error("Error configuring MercadoPago SDK: {}", e.getMessage());
            throw new RuntimeException("Failed to configure MercadoPago SDK", e);
        }
    }

    @Bean
    public String mercadoPagoPublicKey() {
        return publicKey;
    }
} 