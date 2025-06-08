package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.recitapp.recitapp_api.modules.notification.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String fromPhoneNumber;

    @Value("${twilio.messaging.service.sid:}")
    private String messagingServiceSid;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    @PostConstruct
    public void initTwilio() {
        if (accountSid != null && !accountSid.isEmpty() && authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS initialized successfully with Account SID: {}", accountSid);
            if (messagingServiceSid != null && !messagingServiceSid.isEmpty()) {
                log.info("Messaging Service SID configured: {}", messagingServiceSid);
            }
        } else {
            log.warn("Twilio credentials not configured. SMS service will not be available.");
        }
    }

    @Override
    public String sendSms(String to, String messageText) {
        if (!isTwilioConfigured()) {
            log.error("Twilio not configured. Cannot send SMS.");
            throw new RuntimeException("SMS service not configured");
        }

        if (!isPhoneNumberValid(to)) {
            log.error("Invalid phone number format: {}", to);
            throw new IllegalArgumentException("Invalid phone number format");
        }

        try {
            Message message;
            
            // Usar Messaging Service SID si está disponible (más confiable)
            if (messagingServiceSid != null && !messagingServiceSid.isEmpty()) {
                message = Message.creator(
                    new PhoneNumber(to),
                    (String) null, // from será null cuando usamos messaging service
                    messageText
                ).setMessagingServiceSid(messagingServiceSid).create();
                log.debug("SMS sent using Messaging Service SID: {}", messagingServiceSid);
            } else {
                message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    messageText
                ).create();
                log.debug("SMS sent using phone number: {}", fromPhoneNumber);
            }

            log.info("SMS sent successfully to: {}, SID: {}, Status: {}", to, message.getSid(), message.getStatus());
            return message.getSid();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    public void sendBulkSms(List<String> phoneNumbers, String messageText) {
        if (!isTwilioConfigured()) {
            log.error("Twilio not configured. Cannot send bulk SMS.");
            throw new RuntimeException("SMS service not configured");
        }

        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            log.warn("No phone numbers provided for bulk SMS");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (String phoneNumber : phoneNumbers) {
            try {
                sendSms(phoneNumber, messageText);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            }
        }

        log.info("Bulk SMS completed. Success: {}, Failure: {}", successCount, failureCount);
    }

    @Override
    public String sendTemplateSms(String to, String templateId, String... parameters) {
        if (!isTwilioConfigured()) {
            log.error("Twilio not configured. Cannot send template SMS.");
            throw new RuntimeException("SMS service not configured");
        }

        // Para este ejemplo, usaremos plantillas simples
        String messageText = buildMessageFromTemplate(templateId, parameters);
        return sendSms(to, messageText);
    }

    @Override
    public boolean isPhoneNumberValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phoneNumber.trim()).matches();
    }

    private boolean isTwilioConfigured() {
        return accountSid != null && !accountSid.isEmpty() && 
               authToken != null && !authToken.isEmpty() && 
               fromPhoneNumber != null && !fromPhoneNumber.isEmpty();
    }

    private String buildMessageFromTemplate(String templateId, String... parameters) {
        // Plantillas simples para RecitApp
        switch (templateId) {
            case "NEW_EVENT":
                return String.format("¡Nuevo evento! %s en %s. ¡No te lo pierdas!", 
                    parameters.length > 0 ? parameters[0] : "Evento", 
                    parameters.length > 1 ? parameters[1] : "Venue");
            
            case "LOW_AVAILABILITY":
                return String.format("¡Pocas entradas disponibles! Solo quedan %s entradas para %s", 
                    parameters.length > 0 ? parameters[0] : "pocas", 
                    parameters.length > 1 ? parameters[1] : "el evento");
            
            case "EVENT_REMINDER":
                return String.format("Recordatorio: %s es mañana a las %s. ¡Te esperamos!", 
                    parameters.length > 0 ? parameters[0] : "Tu evento", 
                    parameters.length > 1 ? parameters[1] : "hora programada");
            
            case "EVENT_CANCELLED":
                return String.format("Lamentamos informarte que %s ha sido cancelado. Se procesará el reembolso automáticamente.", 
                    parameters.length > 0 ? parameters[0] : "el evento");
            
            case "VERIFICATION_CODE":
                return String.format("Tu código de verificación para RecitApp es: %s", 
                    parameters.length > 0 ? parameters[0] : "123456");
            
            default:
                return String.join(" ", parameters);
        }
    }
} 