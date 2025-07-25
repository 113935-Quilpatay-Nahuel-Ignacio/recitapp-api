package com.recitapp.recitapp_api.modules.notification.service.impl;

/**
 * WhatsApp Service Implementation using Twilio
 * 
 * IMPORTANT SETUP REQUIREMENTS:
 * 1. Configure Twilio WhatsApp Sandbox:
 *    - Go to Twilio Console > Messaging > Try it out > Send a WhatsApp message
 *    - Follow the sandbox setup instructions
 *    - Add recipient phone numbers to the sandbox (they must send a specific message first)
 * 
 * 2. Configuration properties required:
 *    - twilio.whatsapp.account.sid: Your Twilio Account SID
 *    - twilio.whatsapp.auth.token: Your Twilio Auth Token
 *    - twilio.whatsapp.number: Twilio WhatsApp number (e.g., whatsapp:+14155238886)
 * 
 * 3. For production: You need a Twilio WhatsApp Business API account
 */

import com.recitapp.recitapp_api.modules.notification.service.WhatsAppService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;



// @Service
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    @Value("${twilio.whatsapp.account.sid:}")
    private String whatsappAccountSid;

    @Value("${twilio.whatsapp.auth.token:}")
    private String whatsappAuthToken;

    @Value("${twilio.whatsapp.number:}")
    private String whatsappNumber;

    @PostConstruct
    public void initTwilio() {
        log.info("Inicializando WhatsApp Service...");
        log.info("Account SID: {}", whatsappAccountSid != null ? whatsappAccountSid.substring(0, Math.min(10, whatsappAccountSid.length())) + "..." : "null");
        log.info("Auth Token: {}", whatsappAuthToken != null ? "***[CONFIGURED]***" : "null");
        log.info("WhatsApp Number: {}", whatsappNumber);
        
        if (whatsappAccountSid != null && !whatsappAccountSid.isEmpty() && 
            whatsappAuthToken != null && !whatsappAuthToken.isEmpty()) {
            try {
                // Inicializar Twilio con las credenciales de WhatsApp
                Twilio.init(whatsappAccountSid, whatsappAuthToken);
                log.info("✅ Twilio WhatsApp Service inicializado exitosamente");
                log.info("📱 WhatsApp Number configurado: {}", whatsappNumber);
            } catch (Exception e) {
                log.error("❌ Error inicializando Twilio WhatsApp: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ Credenciales de WhatsApp no configuradas. Servicio WhatsApp no disponible.");
        }
    }

    @Override
    public boolean sendWhatsAppMessage(String phoneNumber, String message) {
        try {
            // Validar que el servicio esté configurado
            if (whatsappAccountSid == null || whatsappAccountSid.isEmpty() || 
                whatsappAuthToken == null || whatsappAuthToken.isEmpty() ||
                whatsappNumber == null || whatsappNumber.isEmpty()) {
                log.error("WhatsApp service not properly configured. Cannot send message to {}", phoneNumber);
                return false;
            }
            
            // Formatear número para WhatsApp
            String formattedNumber = formatWhatsAppNumber(phoneNumber);
            
            log.debug("Sending WhatsApp message from {} to {} with message: {}", 
                     whatsappNumber, formattedNumber, message.substring(0, Math.min(50, message.length())));
            
            Message twilioMessage = Message.creator(
                new PhoneNumber(formattedNumber),
                new PhoneNumber(whatsappNumber),
                message
            ).create();

            log.info("Mensaje WhatsApp enviado exitosamente. SID: {} a número: {}", 
                     twilioMessage.getSid(), phoneNumber);
            return true;

        } catch (Exception e) {
            log.error("Error enviando mensaje WhatsApp a {}: {}", phoneNumber, e.getMessage());
            log.error("Detalles del error: From={}, To={}, Exception type: {}", 
                     whatsappNumber, formatWhatsAppNumber(phoneNumber), e.getClass().getSimpleName());
            
            // Proporcionar información específica sobre errores comunes
            if (e.getMessage().contains("Channel with the specified From address")) {
                log.error("❌ SOLUCIÓN: El número WhatsApp '{}' no está configurado correctamente en Twilio.", whatsappNumber);
                log.error("📋 PASOS PARA SOLUCIONARLO:");
                log.error("   1. Ve a Twilio Console > Messaging > Try it out > Send a WhatsApp message");
                log.error("   2. Configura el WhatsApp Sandbox");
                log.error("   3. Verifica que el número '{}' esté habilitado para WhatsApp", whatsappNumber);
            }
            
            return false;
        }
    }

    @Override
    public boolean sendTemplateMessage(String phoneNumber, String templateName, Map<String, String> parameters) {
        try {
            String formattedNumber = formatWhatsAppNumber(phoneNumber);
            
            // Construir mensaje desde template
            String message = buildTemplateMessage(templateName, parameters);
            
            Message twilioMessage = Message.creator(
                new PhoneNumber(formattedNumber),
                new PhoneNumber(whatsappNumber),
                message
            ).create();

            log.info("Mensaje template WhatsApp enviado. SID: {} Template: {} a: {}", 
                     twilioMessage.getSid(), templateName, phoneNumber);
            return true;

        } catch (Exception e) {
            log.error("Error enviando template WhatsApp a {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendMediaMessage(String phoneNumber, String message, String mediaUrl, String mediaType) {
        try {
            String formattedNumber = formatWhatsAppNumber(phoneNumber);
            
            Message twilioMessage = Message.creator(
                new PhoneNumber(formattedNumber),
                new PhoneNumber(whatsappNumber),
                message
            ).setMediaUrl(mediaUrl).create();

            log.info("Mensaje WhatsApp con media enviado. SID: {} Media: {} a: {}", 
                     twilioMessage.getSid(), mediaUrl, phoneNumber);
            return true;

        } catch (Exception e) {
            log.error("Error enviando media WhatsApp a {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendInteractiveMessage(String phoneNumber, String message, List<WhatsAppButton> buttons) {
        try {
            String formattedNumber = formatWhatsAppNumber(phoneNumber);
            
            // Para botones, construir mensaje con opciones
            StringBuilder interactiveMessage = new StringBuilder(message);
            interactiveMessage.append("\n\n🔹 Opciones:");
            
            for (int i = 0; i < buttons.size(); i++) {
                WhatsAppButton button = buttons.get(i);
                interactiveMessage.append("\n").append(i + 1).append(". ").append(button.getTitle());
                if ("url".equals(button.getType()) && button.getUrl() != null) {
                    interactiveMessage.append(" - ").append(button.getUrl());
                }
            }

            Message twilioMessage = Message.creator(
                new PhoneNumber(formattedNumber),
                new PhoneNumber(whatsappNumber),
                interactiveMessage.toString()
            ).create();

            log.info("Mensaje interactivo WhatsApp enviado. SID: {} a: {}", 
                     twilioMessage.getSid(), phoneNumber);
            return true;

        } catch (Exception e) {
            log.error("Error enviando mensaje interactivo WhatsApp a {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    @Override
    public void sendBulkWhatsAppMessages(List<String> phoneNumbers, String message) {
        log.info("Iniciando envío masivo de WhatsApp a {} números", phoneNumbers.size());
        
        for (String phoneNumber : phoneNumbers) {
            try {
                sendWhatsAppMessage(phoneNumber, message);
                // Pausa para evitar rate limiting
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Error en envío masivo a {}: {}", phoneNumber, e.getMessage());
            }
        }
        
        log.info("Envío masivo WhatsApp completado");
    }

    @Override
    public boolean isWhatsAppNumber(String phoneNumber) {
        // Validación básica de formato
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remover espacios y caracteres especiales
        String cleanNumber = phoneNumber.replaceAll("[^+\\d]", "");
        
        // Verificar que tenga formato internacional
        return cleanNumber.startsWith("+") && cleanNumber.length() >= 10 && cleanNumber.length() <= 15;
    }

    @Override
    public String getMessageStatus(String messageSid) {
        try {
            Message message = Message.fetcher(messageSid).fetch();
            return message.getStatus().toString();
        } catch (Exception e) {
            log.error("Error obteniendo estado del mensaje {}: {}", messageSid, e.getMessage());
            return "ERROR";
        }
    }

    private String formatWhatsAppNumber(String phoneNumber) {
        // Limpiar el número
        String cleanNumber = phoneNumber.replaceAll("[^+\\d]", "");
        
        // Si no tiene +, agregarlo (asumiendo que ya incluye código de país)
        if (!cleanNumber.startsWith("+")) {
            cleanNumber = "+" + cleanNumber;
        }
        
        // Formato WhatsApp
        return "whatsapp:" + cleanNumber;
    }

    private String buildTemplateMessage(String templateName, Map<String, String> parameters) {
        return switch (templateName) {
            case "new-event" -> buildNewEventMessage(parameters);
            case "event-reminder" -> buildReminderMessage(parameters);
            case "low-availability" -> buildLowAvailabilityMessage(parameters);
            case "event-cancelled" -> buildCancellationMessage(parameters);
            case "event-modified" -> buildModificationMessage(parameters);
            case "recommendations" -> buildRecommendationMessage(parameters);
            default -> parameters.getOrDefault("message", "Notificación de RecitApp");
        };
    }

    private String buildNewEventMessage(Map<String, String> parameters) {
        return String.format("""
            🎵 *¡Nuevo Evento en RecitApp!*
            
            📅 *%s*
            🎤 Artista: %s
            📍 Lugar: %s
            📆 Fecha: %s
            
            🎫 ¡Compra tus entradas ahora!
            
            _Mensaje enviado por RecitApp_
            """,
            parameters.getOrDefault("eventName", "Evento"),
            parameters.getOrDefault("artistName", "Por confirmar"),
            parameters.getOrDefault("venueName", "Por confirmar"),
            parameters.getOrDefault("eventDate", "Por confirmar")
        );
    }

    private String buildReminderMessage(Map<String, String> parameters) {
        return String.format("""
            ⏰ *Recordatorio de Evento*
            
            🎵 %s
            📅 Mañana a las %s
            📍 %s
            
            🎫 ¡No olvides tu entrada!
            
            _RecitApp te recuerda_
            """,
            parameters.getOrDefault("eventName", "Tu evento"),
            parameters.getOrDefault("eventTime", "hora por confirmar"),
            parameters.getOrDefault("venueName", "lugar por confirmar")
        );
    }

    private String buildLowAvailabilityMessage(Map<String, String> parameters) {
        return String.format("""
            ⚠️ *¡Últimas Entradas Disponibles!*
            
            🎵 %s
            🎫 Solo quedan %s entradas
            📆 %s
            
            ⚡ ¡Compra antes de que se agoten!
            
            _Alerta de RecitApp_
            """,
            parameters.getOrDefault("eventName", "Evento"),
            parameters.getOrDefault("availableTickets", "pocas"),
            parameters.getOrDefault("eventDate", "fecha por confirmar")
        );
    }

    private String buildCancellationMessage(Map<String, String> parameters) {
        return String.format("""
            ❌ *Evento Cancelado*
            
            🎵 %s
            📅 %s
            
            💰 Tu reembolso será procesado automáticamente
            
            📧 Recibirás más información por email
            
            _Lamentamos las molestias - RecitApp_
            """,
            parameters.getOrDefault("eventName", "Evento"),
            parameters.getOrDefault("eventDate", "fecha")
        );
    }

    private String buildModificationMessage(Map<String, String> parameters) {
        return String.format("""
            🔄 *Evento Modificado*
            
            🎵 %s
            📅 Nueva fecha: %s
            📍 Nuevo lugar: %s
            
            🎫 Tus entradas siguen siendo válidas
            
            _Actualización de RecitApp_
            """,
            parameters.getOrDefault("eventName", "Evento"),
            parameters.getOrDefault("newDate", "fecha por confirmar"),
            parameters.getOrDefault("newVenue", "lugar por confirmar")
        );
    }

    private String buildRecommendationMessage(Map<String, String> parameters) {
        return String.format("""
            💫 *Recomendación Personalizada*
            
            🎵 Te puede gustar: %s
            🎤 Artista: %s
            📅 %s
            
            ✨ Basado en tus gustos musicales
            
            🎫 ¿Te interesa? ¡Mira más detalles!
            
            _Recomendación de RecitApp_
            """,
            parameters.getOrDefault("eventName", "Evento recomendado"),
            parameters.getOrDefault("artistName", "Artista"),
            parameters.getOrDefault("eventDate", "fecha por confirmar")
        );
    }
} 