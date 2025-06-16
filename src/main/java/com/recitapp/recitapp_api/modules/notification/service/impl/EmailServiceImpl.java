package com.recitapp.recitapp_api.modules.notification.service.impl;

import com.recitapp.recitapp_api.modules.notification.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from.email:recitapp@noreply.com}")
    private String fromEmail;

    @Value("${spring.mail.from.name:Recitapp}")
    private String fromName;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            // Convert simple email to HTML format with logo
            String htmlContent = buildSimpleEmailHtml(subject, text);
            sendHtmlEmail(to, subject, htmlContent);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private String buildSimpleEmailHtml(String subject, String text) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 0 10px rgba(0,0,0,0.1); }");
        html.append(".header { background-color: #22C55E; color: white; padding: 20px; text-align: center; }");
        html.append(".logo { margin-bottom: 15px; }");
        html.append(".logo-circle { display: inline-block; width: 48px; height: 48px; background-color: #1a9e4a; border-radius: 50%; line-height: 48px; text-align: center; font-size: 24px; font-weight: bold; color: white; margin: 0 auto; }");
        html.append(".content { padding: 30px; }");
        html.append(".footer { text-align: center; padding: 20px; background-color: #f8f9fa; color: #666; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<div class='logo'>");
        html.append("<div class='logo-circle'>R</div>");
        html.append("</div>");
        html.append("<h2>").append(subject).append("</h2>");
        html.append("</div>");
        
        html.append("<div class='content'>");
        html.append("<p>").append(text).append("</p>");
        html.append("</div>");
        
        html.append("<div class='footer'>");
        html.append("<p>Este es un email automático de Recitapp</p>");
        html.append("<p>© 2024 Recitapp. Todos los derechos reservados.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    @Override
    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            
            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);
            
            log.info("Template email sent successfully to: {} using template: {}", to, templateName);
        } catch (Exception e) {
            log.error("Failed to send template email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send template email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indica que es HTML
            
            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentPath, String attachmentName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            
            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment(attachmentName, file);
            
            mailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    @Override
    public void sendNewEventEmail(String recipientEmail, String eventName, String artistName, String eventDate, String venueName) {
        try {
            Map<String, Object> variables = Map.of(
                "eventName", eventName,
                "artistName", artistName,
                "eventDate", eventDate,
                "venueName", venueName,
                "userName", "Usuario"
            );
            
            sendTemplateEmail(
                recipientEmail,
                "🎵 Nuevo Evento Disponible: " + eventName,
                "email/new-event",
                variables
            );
            
            log.info("New event email sent successfully to: {} for event: {}", recipientEmail, eventName);
        } catch (Exception e) {
            log.error("Failed to send new event email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send new event email", e);
        }
    }

    @Override
    public void sendLowAvailabilityEmail(String recipientEmail, String eventName, Integer ticketsRemaining) {
        try {
            Map<String, Object> variables = Map.of(
                "eventName", eventName,
                "ticketsRemaining", ticketsRemaining,
                "userName", "Usuario",
                "venueName", "Venue por confirmar",
                "eventDate", "Fecha por confirmar"
            );
            
            sendTemplateEmail(
                recipientEmail,
                "⚠️ Pocas Entradas Disponibles: " + eventName,
                "email/low-availability",
                variables
            );
            
            log.info("Low availability email sent successfully to: {} for event: {}", recipientEmail, eventName);
        } catch (Exception e) {
            log.error("Failed to send low availability email to {}: {}", recipientEmail, e.getMessage());
            throw new RuntimeException("Failed to send low availability email", e);
        }
    }
} 