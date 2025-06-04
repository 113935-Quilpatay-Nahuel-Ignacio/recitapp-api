package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.io.image.ImageDataFactory;
import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;
import com.recitapp.recitapp_api.modules.ticket.service.TicketPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class TicketPdfServiceImpl implements TicketPdfService {

    @Value("${app.ticket.pdf.storage.path:./tickets}")
    private String pdfStoragePath;

    @Override
    public byte[] generateTicketPdf(TicketDTO ticket) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            Paragraph title = new Paragraph("ENTRADA DIGITAL")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Información del evento
            Paragraph eventInfo = new Paragraph(ticket.getEventName() != null ? ticket.getEventName() : "Evento")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(eventInfo);

            // Información del venue
            if (ticket.getVenueName() != null) {
                Paragraph venueInfo = new Paragraph("Lugar: " + ticket.getVenueName())
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
                document.add(venueInfo);
            }

            // Fecha del evento
            if (ticket.getEventDate() != null) {
                Paragraph dateInfo = new Paragraph("Fecha: " + ticket.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER);
                document.add(dateInfo);
            }

            // Tabla con información del ticket
            Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            
            table.addCell("Asistente:");
            table.addCell((ticket.getAttendeeFirstName() != null ? ticket.getAttendeeFirstName() : "") + " " + 
                         (ticket.getAttendeeLastName() != null ? ticket.getAttendeeLastName() : ""));
            
            table.addCell("DNI:");
            table.addCell(ticket.getAttendeeDni() != null ? ticket.getAttendeeDni() : "");
            
            table.addCell("Sección:");
            table.addCell(ticket.getSectionName() != null ? ticket.getSectionName() : "General");
            
            table.addCell("Precio:");
            table.addCell("$" + (ticket.getPrice() != null ? ticket.getPrice().toString() : "0.00"));
            
            table.addCell("Estado:");
            table.addCell(ticket.getStatus() != null ? ticket.getStatus() : "ACTIVO");
            
            if (ticket.getPurchaseDate() != null) {
                table.addCell("Fecha de compra:");
                table.addCell(ticket.getPurchaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }

            document.add(table);

            // Generar y agregar código QR
            if (ticket.getQrCode() != null) {
                try {
                    String qrData = ticket.getQrCode();
                    
                    // Si el QR code ya contiene "data:image/png;base64," extraer solo el texto relevante
                    if (qrData.startsWith("data:image/png;base64,")) {
                        // Para efectos del QR, usar el identification code o datos más simples
                        qrData = "TICKET:" + (ticket.getId() != null ? ticket.getId() : "") + 
                                "|EVENT:" + (ticket.getEventId() != null ? ticket.getEventId() : "") +
                                "|CODE:" + (ticket.getQrCode().contains("TKT-") ? 
                                          ticket.getQrCode().substring(ticket.getQrCode().indexOf("TKT-")) : 
                                          ticket.getQrCode());
                    }
                    
                    byte[] qrCodeImage = generateQRCodeImage(qrData, 200, 200);
                    Image qrImage = new Image(ImageDataFactory.create(qrCodeImage));
                    qrImage.setTextAlignment(TextAlignment.CENTER);
                    qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    
                    Paragraph qrTitle = new Paragraph("Código QR de validación:")
                        .setFontSize(12)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER);
                    document.add(qrTitle);
                    document.add(qrImage);
                    
                    // Agregar texto del código de identificación
                    if (qrData.contains("TKT-")) {
                        String identificationCode = qrData.substring(qrData.indexOf("TKT-"));
                        if (identificationCode.contains("|")) {
                            identificationCode = identificationCode.substring(0, identificationCode.indexOf("|"));
                        }
                        Paragraph codeText = new Paragraph("Código de identificación: " + identificationCode)
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.CENTER);
                        document.add(codeText);
                    }
                    
                } catch (Exception e) {
                    log.error("Error generating QR code for ticket: {}", e.getMessage());
                    Paragraph qrError = new Paragraph("Error generando código QR")
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER);
                    document.add(qrError);
                }
            }

            // Información adicional
            Paragraph footer = new Paragraph("Conserve esta entrada para el acceso al evento. Válida únicamente para la fecha y hora indicadas.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for ticket: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating ticket PDF", e);
        }
    }

    @Override
    public String saveTicketPdf(TicketDTO ticket) {
        try {
            byte[] pdfBytes = generateTicketPdf(ticket);
            
            // Crear directorio si no existe
            Path storageDir = Paths.get(pdfStoragePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            
            // Generar nombre de archivo único
            String fileName = "ticket_" + ticket.getId() + "_" + System.currentTimeMillis() + ".pdf";
            Path filePath = storageDir.resolve(fileName);
            
            // Guardar archivo
            Files.write(filePath, pdfBytes);
            
            log.info("Ticket PDF saved: {}", filePath.toString());
            return filePath.toString();
            
        } catch (Exception e) {
            log.error("Error saving ticket PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving ticket PDF", e);
        }
    }

    private byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
} 