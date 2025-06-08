package com.recitapp.recitapp_api.modules.notification.service;

import java.util.List;

public interface SmsService {
    
    /**
     * Envía un SMS simple a un número de teléfono
     */
    String sendSms(String to, String message);
    
    /**
     * Envía un SMS a múltiples números
     */
    void sendBulkSms(List<String> phoneNumbers, String message);
    
    /**
     * Envía un SMS con plantilla
     */
    String sendTemplateSms(String to, String templateId, String... parameters);
    
    /**
     * Verifica si un número de teléfono es válido
     */
    boolean isPhoneNumberValid(String phoneNumber);
} 