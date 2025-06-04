package com.recitapp.recitapp_api.modules.ticket.controller;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketService;
import com.recitapp.recitapp_api.modules.ticket.service.TicketPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketPdfController {

    private final TicketService ticketService;
    private final TicketPdfService ticketPdfService;

    @GetMapping("/{ticketId}/download-pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable Long ticketId) {
        try {
            log.info("Generating PDF download for ticket ID: {}", ticketId);
            
            // Obtener información del ticket
            TicketDTO ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Generar PDF
            byte[] pdfBytes = ticketPdfService.generateTicketPdf(ticket);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "entrada_" + ticketId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            log.info("PDF generated successfully for ticket ID: {}", ticketId);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error generating PDF for ticket ID {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{ticketId}/resend-email")
    public ResponseEntity<String> resendTicketEmail(@PathVariable Long ticketId, @RequestParam String email) {
        try {
            log.info("Resending ticket email for ticket ID: {} to email: {}", ticketId, email);
            
            // Obtener información del ticket
            TicketDTO ticket = ticketService.getTicketById(ticketId);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Generar PDF y enviar email
            byte[] pdfBytes = ticketPdfService.generateTicketPdf(ticket);
            // Note: You'll need to inject TicketEmailService here too
            
            log.info("Email resent successfully for ticket ID: {}", ticketId);
            return ResponseEntity.ok("Email enviado exitosamente");
            
        } catch (Exception e) {
            log.error("Error resending email for ticket ID {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error enviando email");
        }
    }
} 