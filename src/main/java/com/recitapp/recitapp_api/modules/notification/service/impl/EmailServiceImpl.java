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
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromEmail + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
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