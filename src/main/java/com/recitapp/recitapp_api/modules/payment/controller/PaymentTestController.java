package com.recitapp.recitapp_api.modules.payment.controller;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/payments")
public class PaymentTestController {

    @Value("${mercadopago.access.token}")
    private String accessToken;
    
    @Value("${mercadopago.public.key}")
    private String publicKey;

    @GetMapping("/test")
    public String testEndpoint() {
        System.out.println("=== PAYMENT TEST ENDPOINT CALLED ===");
        return "Payment endpoints are working without authentication!";
    }

    @PostMapping("/test-post")
    public String testPostEndpoint(@RequestBody(required = false) String body) {
        System.out.println("=== PAYMENT TEST POST ENDPOINT CALLED ===");
        System.out.println("Body: " + body);
        return "Payment POST endpoints are working without authentication!";
    }
    
    @GetMapping("/test-config")
    public String testMercadoPagoConfig() {
        try {
            System.out.println("=== TESTING MERCADOPAGO CONFIGURATION ===");
            
            // Check if access token is configured
            String currentAccessToken = MercadoPagoConfig.getAccessToken();
            System.out.println("Current Access Token configured: " + (currentAccessToken != null ? "YES" : "NO"));
            if (currentAccessToken != null) {
                System.out.println("Access Token starts with: " + currentAccessToken.substring(0, Math.min(20, currentAccessToken.length())) + "...");
            }
            
            // Check environment variables
            System.out.println("Access Token from properties: " + (accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "NULL"));
            System.out.println("Public Key from properties: " + (publicKey != null ? publicKey.substring(0, Math.min(20, publicKey.length())) + "..." : "NULL"));
            
            return String.format("MercadoPago Config Test - Access Token: %s, Public Key: %s", 
                    currentAccessToken != null ? "CONFIGURED" : "NOT_CONFIGURED",
                    publicKey != null ? "CONFIGURED" : "NOT_CONFIGURED");
        } catch (Exception e) {
            System.out.println("Error testing MercadoPago config: " + e.getMessage());
            return "Error testing MercadoPago config: " + e.getMessage();
        }
    }
} 