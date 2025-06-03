package com.recitapp.recitapp_api.modules.payment.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentTestController {

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
} 