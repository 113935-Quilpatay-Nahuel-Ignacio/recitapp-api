package com.recitapp.recitapp_api.modules.notification.service;

import java.util.List;
import java.util.Map;

public interface WhatsAppService {
    
    /**
     * Envía un mensaje de WhatsApp simple
     */
    boolean sendWhatsAppMessage(String phoneNumber, String message);
    
    /**
     * Envía un mensaje con template
     */
    boolean sendTemplateMessage(String phoneNumber, String templateName, Map<String, String> parameters);
    
    /**
     * Envía un mensaje con media (imagen, documento)
     */
    boolean sendMediaMessage(String phoneNumber, String message, String mediaUrl, String mediaType);
    
    /**
     * Envía un mensaje con botones interactivos
     */
    boolean sendInteractiveMessage(String phoneNumber, String message, List<WhatsAppButton> buttons);
    
    /**
     * Envía mensajes en lote
     */
    void sendBulkWhatsAppMessages(List<String> phoneNumbers, String message);
    
    /**
     * Valida si un número de teléfono es válido para WhatsApp
     */
    boolean isWhatsAppNumber(String phoneNumber);
    
    /**
     * Obtiene el estado de entrega de un mensaje
     */
    String getMessageStatus(String messageSid);
    
    /**
     * Clase para botones de WhatsApp
     */
    class WhatsAppButton {
        private String id;
        private String title;
        private String type; // "quick_reply" o "url"
        private String payload; // Para quick_reply
        private String url; // Para type url
        
        public WhatsAppButton(String id, String title, String type) {
            this.id = id;
            this.title = title;
            this.type = type;
        }
        
        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
} 