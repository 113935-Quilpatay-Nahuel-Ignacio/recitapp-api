package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketEmailServiceImpl implements TicketEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from.email:recitapp@noreply.com}")
    private String fromEmail;

    @Value("${spring.mail.from.name:Recitapp}")
    private String fromName;

    @Value("${app.name:RecitApp}")
    private String appName;

    @Override
    public void sendTicketByEmail(TicketDTO ticket, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Tu entrada para " + (ticket.getEventName() != null ? ticket.getEventName() : "el evento"));

            String htmlContent = buildEmailContent(ticket);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Ticket email sent successfully to: {}", recipientEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending ticket email to {}: {}", recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Error sending ticket email", e);
        }
    }

    @Override
    public void sendTicketWithAttachment(TicketDTO ticket, String recipientEmail, byte[] pdfAttachment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Tu entrada para " + (ticket.getEventName() != null ? ticket.getEventName() : "el evento"));

            String htmlContent = buildEmailContent(ticket);
            helper.setText(htmlContent, true);

            // Agregar PDF como adjunto
            String fileName = "entrada_" + ticket.getId() + ".pdf";
            helper.addAttachment(fileName, new ByteArrayResource(pdfAttachment));

            mailSender.send(message);
            log.info("Ticket email with PDF attachment sent successfully to: {}", recipientEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending ticket email with attachment to {}: {}", recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Error sending ticket email with attachment", e);
        }
    }

    private String buildEmailContent(TicketDTO ticket) {
        StringBuilder content = new StringBuilder();
        
        content.append("<!DOCTYPE html>");
        content.append("<html>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<style>");
        content.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        content.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        content.append(".header { background-color: #22C55E; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }");
        content.append(".logo { display: inline-block; margin-bottom: 10px; }");
        content.append(".logo-circle { display: inline-block; width: 48px; height: 48px; background-color: #1a9e4a; border-radius: 50%; line-height: 48px; text-align: center; font-size: 24px; font-weight: bold; color: white; margin: 0 auto; }");
        content.append(".content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; }");
        content.append(".ticket-info { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; }");
        content.append(".info-row { display: flex; justify-content: space-between; margin: 8px 0; padding: 8px 0; border-bottom: 1px solid #eee; }");
        content.append(".label { font-weight: bold; color: #555; }");
        content.append(".value { color: #333; }");
        content.append(".footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }");
        content.append("</style>");
        content.append("</head>");
        content.append("<body>");
        
        content.append("<div class='container'>");
        content.append("<div class='header'>");
        content.append("<div class='logo'>");
        content.append("<div class='logo-circle'>R</div>");
        content.append("</div>");
        content.append("<h1>¡Tu entrada está lista!</h1>");
        content.append("<p>Gracias por tu compra en ").append(appName).append("</p>");
        content.append("</div>");
        
        content.append("<div class='content'>");
        content.append("<h2>Detalles de tu entrada</h2>");
        
        content.append("<div class='ticket-info'>");
        
        if (ticket.getEventName() != null) {
            content.append("<div class='info-row'>");
            content.append("<span class='label'>Evento:</span>");
            content.append("<span class='value'>").append(ticket.getEventName()).append("</span>");
            content.append("</div>");
        }
        
        if (ticket.getVenueName() != null) {
            content.append("<div class='info-row'>");
            content.append("<span class='label'>Lugar:</span>");
            content.append("<span class='value'>").append(ticket.getVenueName()).append("</span>");
            content.append("</div>");
        }
        
        if (ticket.getEventDate() != null) {
            content.append("<div class='info-row'>");
            content.append("<span class='label'>Fecha y hora:</span>");
            content.append("<span class='value'>").append(ticket.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</span>");
            content.append("</div>");
        }
        
        content.append("<div class='info-row'>");
        content.append("<span class='label'>Asistente:</span>");
        content.append("<span class='value'>").append(ticket.getAttendeeFirstName() != null ? ticket.getAttendeeFirstName() : "").append(" ").append(ticket.getAttendeeLastName() != null ? ticket.getAttendeeLastName() : "").append("</span>");
        content.append("</div>");
        
        if (ticket.getAttendeeDni() != null) {
            content.append("<div class='info-row'>");
            content.append("<span class='label'>DNI:</span>");
            content.append("<span class='value'>").append(ticket.getAttendeeDni()).append("</span>");
            content.append("</div>");
        }
        
        content.append("<div class='info-row'>");
        content.append("<span class='label'>Número de entrada:</span>");
        content.append("<span class='value'>#").append(ticket.getId()).append("</span>");
        content.append("</div>");
        
        if (ticket.getPrice() != null) {
            content.append("<div class='info-row'>");
            content.append("<span class='label'>Precio:</span>");
            content.append("<span class='value'>$").append(ticket.getPrice()).append("</span>");
            content.append("</div>");
        }
        
        content.append("</div>");
        
        content.append("<p><strong>Importante:</strong> Presenta esta entrada (digital o impresa) en el evento para ingresar.</p>");
        
        content.append("</div>");
        
        content.append("<div class='footer'>");
        content.append("<p>¡Disfruta el evento!</p>");
        content.append("<p>© 2024 ").append(appName).append(". Todos los derechos reservados.</p>");
        content.append("</div>");
        
        content.append("</div>");
        content.append("</body>");
        content.append("</html>");
        
        return content.toString();
    }
} 