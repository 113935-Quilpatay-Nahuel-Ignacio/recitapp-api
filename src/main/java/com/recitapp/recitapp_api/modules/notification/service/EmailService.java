package com.recitapp.recitapp_api.modules.notification.service;

import java.util.Map;

public interface EmailService {
    
    /**
     * Envía un email simple con texto plano
     */
    void sendSimpleEmail(String to, String subject, String text);
    
    /**
     * Envía un email con plantilla HTML y variables
     */
    void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables);
    
    /**
     * Envía un email HTML sin plantilla
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * Envía un email con adjuntos
     */
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentPath, String attachmentName);
    
    /**
     * Envía email de nuevo evento usando plantilla
     */
    void sendNewEventEmail(String recipientEmail, String eventName, String artistName, String eventDate, String venueName);
    
    /**
     * Envía email de baja disponibilidad usando plantilla
     */
    void sendLowAvailabilityEmail(String recipientEmail, String eventName, Integer ticketsRemaining);
}
