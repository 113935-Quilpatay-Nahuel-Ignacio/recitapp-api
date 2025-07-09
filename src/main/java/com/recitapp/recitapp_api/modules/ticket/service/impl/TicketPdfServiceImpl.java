package com.recitapp.recitapp_api.modules.ticket.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
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

    // Colores corporativos
    private static final Color PRIMARY_DARK = new DeviceRgb(45, 45, 45);      // #2D2D2D
    private static final Color SECONDARY_DARK = new DeviceRgb(26, 26, 26);    // #1A1A1A
    private static final Color ACCENT_GREEN = new DeviceRgb(34, 197, 94);     // #22C55E
    private static final Color WHITE = new DeviceRgb(255, 255, 255);
    private static final Color LIGHT_GRAY = new DeviceRgb(243, 244, 246);    // #F3F4F6
    private static final Color TEXT_GRAY = new DeviceRgb(107, 114, 128);     // #6B7280

    @Override
    public byte[] generateTicketPdf(TicketDTO ticket) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Configurar márgenes optimizados para una página
            document.setMargins(18, 18, 18, 18);

            // Fuentes
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // === HEADER PRINCIPAL ===
            createOptimalHeader(document, boldFont, regularFont);

            // === INFORMACIÓN DEL EVENTO Y ASISTENTE EN COLUMNAS ===
            createOptimalMainContentSection(document, ticket, boldFont, regularFont);

            // === SECCIÓN QR CODE OPTIMAL ===
            createOptimalQRSection(document, ticket, boldFont, regularFont);

            // === FOOTER OPTIMAL ===
            createOptimalFooter(document, regularFont);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF for ticket: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating ticket PDF", e);
        }
    }

    private void createOptimalHeader(Document document, PdfFont boldFont, PdfFont regularFont) {
        // Header balanceado
        Table headerTable = new Table(1);
        headerTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell headerCell = new Cell()
            .add(new Paragraph("ENTRADA DIGITAL")
                .setFont(boldFont)
                .setFontSize(26)
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5))
            .add(new Paragraph("RecitApp")
                .setFont(regularFont)
                .setFontSize(14)
                .setFontColor(ACCENT_GREEN)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(0))
            .setBackgroundColor(PRIMARY_DARK)
            .setBorder(Border.NO_BORDER)
            .setPadding(18);

        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Espacio controlado
        document.add(new Paragraph(" ").setMarginTop(12).setMarginBottom(10));
    }

    private void createOptimalMainContentSection(Document document, TicketDTO ticket, PdfFont boldFont, PdfFont regularFont) {
        // Tabla principal con 2 columnas balanceada
        Table mainTable = new Table(2);
        mainTable.setWidth(UnitValue.createPercentValue(100));

        // === COLUMNA IZQUIERDA: INFORMACIÓN DEL EVENTO ===
        Cell eventColumn = new Cell();
        
        // Nombre del evento balanceado
        Paragraph eventTitle = new Paragraph(ticket.getEventName() != null ? ticket.getEventName() : "Evento")
            .setFont(boldFont)
            .setFontSize(18)
            .setFontColor(PRIMARY_DARK)
            .setTextAlignment(TextAlignment.CENTER)
            .setBackgroundColor(LIGHT_GRAY)
            .setBorder(new SolidBorder(ACCENT_GREEN, 2))
            .setPadding(15)
            .setMarginBottom(12);
        
        eventColumn.add(eventTitle);

        // Información del evento balanceada
        Table eventInfoTable = new Table(2);
        eventInfoTable.setWidth(UnitValue.createPercentValue(100));

        if (ticket.getVenueName() != null) {
            eventInfoTable.addCell(createOptimalInfoCell("📍 Lugar:", boldFont));
            eventInfoTable.addCell(createOptimalInfoValueCell(ticket.getVenueName(), regularFont));
        }

        if (ticket.getEventDate() != null) {
            eventInfoTable.addCell(createOptimalInfoCell("📅 Fecha:", boldFont));
            eventInfoTable.addCell(createOptimalInfoValueCell(
                ticket.getEventDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
                regularFont));
            
            eventInfoTable.addCell(createOptimalInfoCell("🕐 Hora:", boldFont));
            eventInfoTable.addCell(createOptimalInfoValueCell(
                ticket.getEventDate().format(DateTimeFormatter.ofPattern("HH:mm")), 
                regularFont));
        }

        eventColumn.add(eventInfoTable);
        eventColumn.setBorder(Border.NO_BORDER);
        eventColumn.setPadding(8);

        // === COLUMNA DERECHA: INFORMACIÓN DEL ASISTENTE ===
        Cell attendeeColumn = new Cell();
        
        // Header de asistente balanceado
        Paragraph attendeeTitle = new Paragraph("INFORMACIÓN DEL ASISTENTE")
            .setFont(boldFont)
            .setFontSize(14)
            .setFontColor(WHITE)
            .setBackgroundColor(SECONDARY_DARK)
            .setPadding(12)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(0);
        
        attendeeColumn.add(attendeeTitle);

        // Tabla de asistente balanceada
        Table attendeeTable = new Table(2);
        attendeeTable.setWidth(UnitValue.createPercentValue(100));
        attendeeTable.setBorder(new SolidBorder(SECONDARY_DARK, 1));

        attendeeTable.addCell(createOptimalStyledInfoCell("👤 Asistente:", boldFont));
        attendeeTable.addCell(createOptimalStyledInfoValueCell(
            (ticket.getAttendeeFirstName() != null ? ticket.getAttendeeFirstName() : "") + " " + 
            (ticket.getAttendeeLastName() != null ? ticket.getAttendeeLastName() : ""), 
            regularFont));

        attendeeTable.addCell(createOptimalStyledInfoCell("🆔 DNI:", boldFont));
        attendeeTable.addCell(createOptimalStyledInfoValueCell(
            ticket.getAttendeeDni() != null ? ticket.getAttendeeDni() : "", regularFont));

        attendeeTable.addCell(createOptimalStyledInfoCell("🎫 Sección:", boldFont));
        attendeeTable.addCell(createOptimalStyledInfoValueCell(
            ticket.getSectionName() != null ? ticket.getSectionName() : "General", regularFont));

        attendeeTable.addCell(createOptimalStyledInfoCell("💰 Precio:", boldFont));
        attendeeTable.addCell(createOptimalStyledInfoValueCell(
            "$" + (ticket.getPrice() != null ? String.format("%.2f", ticket.getPrice()) : "0.00"), 
            regularFont));

        attendeeTable.addCell(createOptimalStyledInfoCell("📊 Estado:", boldFont));
        Cell statusCell = createOptimalStyledInfoValueCell(
            ticket.getStatus() != null ? ticket.getStatus() : "ACTIVO", regularFont);
        
        if ("ACTIVO".equals(ticket.getStatus())) {
            statusCell.setFontColor(ACCENT_GREEN);
        }
        attendeeTable.addCell(statusCell);

        if (ticket.getPurchaseDate() != null) {
            attendeeTable.addCell(createOptimalStyledInfoCell("🛒 Compra:", boldFont));
            attendeeTable.addCell(createOptimalStyledInfoValueCell(
                ticket.getPurchaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                regularFont));
        }

        attendeeColumn.add(attendeeTable);
        attendeeColumn.setBorder(Border.NO_BORDER);
        attendeeColumn.setPadding(8);

        // Agregar columnas a la tabla principal
        mainTable.addCell(eventColumn);
        mainTable.addCell(attendeeColumn);
        
        document.add(mainTable);
        document.add(new Paragraph(" ").setMarginTop(18).setMarginBottom(15));
    }

    private void createOptimalQRSection(Document document, TicketDTO ticket, PdfFont boldFont, PdfFont regularFont) {
        if (ticket.getQrCode() != null) {
            try {
                // Tabla horizontal para QR + información balanceada
                Table qrTable = new Table(2);
                qrTable.setWidth(UnitValue.createPercentValue(100));

                // === COLUMNA IZQUIERDA: CÓDIGO QR ===
                Cell qrCell = new Cell();
                
                // Preparar datos del QR
                String qrData = ticket.getQrCode();
                if (qrData.startsWith("data:image/png;base64,")) {
                    qrData = "TICKET:" + (ticket.getId() != null ? ticket.getId() : "") + 
                            "|EVENT:" + (ticket.getEventId() != null ? ticket.getEventId() : "") +
                            "|CODE:" + (ticket.getQrCode().contains("TKT-") ? 
                                      ticket.getQrCode().substring(ticket.getQrCode().indexOf("TKT-")) : 
                                      ticket.getQrCode());
                }

                // Generar QR de tamaño balanceado
                byte[] qrCodeImage = generateQRCodeImage(qrData, 160, 160);
                Image qrImage = new Image(ImageDataFactory.create(qrCodeImage));
                qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

                qrCell.add(qrImage);
                qrCell.setBackgroundColor(WHITE);
                qrCell.setBorder(new SolidBorder(ACCENT_GREEN, 2));
                qrCell.setPadding(18);
                qrCell.setTextAlignment(TextAlignment.CENTER);

                // === COLUMNA DERECHA: INFORMACIÓN DEL QR ===
                Cell qrInfoCell = new Cell();
                
                qrInfoCell.add(new Paragraph("CÓDIGO DE VALIDACIÓN")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setFontColor(WHITE)
                    .setBackgroundColor(ACCENT_GREEN)
                    .setPadding(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(12));

                qrInfoCell.add(new Paragraph("Presenta este código en la entrada del evento")
                    .setFont(regularFont)
                    .setFontSize(12)
                    .setFontColor(PRIMARY_DARK)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(12));

                // Código de identificación balanceado
                if (qrData.contains("TKT-")) {
                    String identificationCode = qrData.substring(qrData.indexOf("TKT-"));
                    if (identificationCode.contains("|")) {
                        identificationCode = identificationCode.substring(0, identificationCode.indexOf("|"));
                    }
                    
                    qrInfoCell.add(new Paragraph("Código de identificación:")
                        .setFont(boldFont)
                        .setFontSize(11)
                        .setFontColor(TEXT_GRAY)
                        .setMarginBottom(6));
                    
                    qrInfoCell.add(new Paragraph(identificationCode)
                        .setFont(regularFont)
                        .setFontSize(13)
                        .setFontColor(PRIMARY_DARK)
                        .setBackgroundColor(LIGHT_GRAY)
                        .setPadding(10)
                        .setTextAlignment(TextAlignment.CENTER));
                }

                qrInfoCell.setBorder(Border.NO_BORDER);
                qrInfoCell.setPadding(12);
                qrInfoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);

                qrTable.addCell(qrCell);
                qrTable.addCell(qrInfoCell);
                
                document.add(qrTable);

            } catch (Exception e) {
                log.error("Error generating QR code for ticket: {}", e.getMessage());
                
                Paragraph qrError = new Paragraph("⚠️ Error generando código QR")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setFontColor(WHITE)
                    .setBackgroundColor(new DeviceRgb(239, 68, 68))
                    .setPadding(12)
                    .setTextAlignment(TextAlignment.CENTER);
                document.add(qrError);
            }
        }
    }

    private void createOptimalFooter(Document document, PdfFont regularFont) {
        document.add(new Paragraph(" ").setMarginTop(18));

        Table footerTable = new Table(1);
        footerTable.setWidth(UnitValue.createPercentValue(100));

        Cell footerCell = new Cell()
            .add(new Paragraph("🎵 Conserve esta entrada para el acceso al evento")
                .setFont(regularFont)
                .setFontSize(11)
                .setFontColor(WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6))
            .add(new Paragraph("Válida únicamente para la fecha y hora indicadas")
                .setFont(regularFont)
                .setFontSize(10)
                .setFontColor(LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6))
            .add(new Paragraph("RecitApp - Tu plataforma de eventos")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ACCENT_GREEN)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(0))
            .setBackgroundColor(PRIMARY_DARK)
            .setBorder(Border.NO_BORDER)
            .setPadding(16);

        footerTable.addCell(footerCell);
        document.add(footerTable);
    }

    // Métodos helper optimales
    private Cell createOptimalInfoCell(String label, PdfFont boldFont) {
        return new Cell()
            .add(new Paragraph(label)
                .setFont(boldFont)
                .setFontSize(11)
                .setFontColor(PRIMARY_DARK))
            .setBorder(Border.NO_BORDER)
            .setPadding(6)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell createOptimalInfoValueCell(String value, PdfFont regularFont) {
        return new Cell()
            .add(new Paragraph(value)
                .setFont(regularFont)
                .setFontSize(11)
                .setFontColor(SECONDARY_DARK))
            .setBorder(Border.NO_BORDER)
            .setPadding(6)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell createOptimalStyledInfoCell(String label, PdfFont boldFont) {
        return new Cell()
            .add(new Paragraph(label)
                .setFont(boldFont)
                .setFontSize(11)
                .setFontColor(PRIMARY_DARK))
            .setBackgroundColor(LIGHT_GRAY)
            .setBorder(new SolidBorder(WHITE, 0.5f))
            .setPadding(9)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell createOptimalStyledInfoValueCell(String value, PdfFont regularFont) {
        return new Cell()
            .add(new Paragraph(value)
                .setFont(regularFont)
                .setFontSize(11)
                .setFontColor(SECONDARY_DARK))
            .setBackgroundColor(WHITE)
            .setBorder(new SolidBorder(LIGHT_GRAY, 0.5f))
            .setPadding(9)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    // Mantener los métodos helper originales pero no utilizados en el diseño compacto
    private Cell createInfoCell(String label, PdfFont boldFont, PdfFont regularFont) {
        return new Cell()
            .add(new Paragraph(label)
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(PRIMARY_DARK))
            .setBorder(Border.NO_BORDER)
            .setPadding(8)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell createInfoValueCell(String value, PdfFont regularFont) {
        return new Cell()
            .add(new Paragraph(value)
                .setFont(regularFont)
                .setFontSize(12)
                .setFontColor(SECONDARY_DARK))
            .setBorder(Border.NO_BORDER)
            .setPadding(8)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell createStyledInfoCell(String label, PdfFont boldFont) {
        return new Cell()
            .add(new Paragraph(label)
                .setFont(boldFont)
                .setFontSize(12)
                .setFontColor(PRIMARY_DARK))
            .setBackgroundColor(LIGHT_GRAY)
            .setBorder(new SolidBorder(WHITE, 1))
            .setPadding(12)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private Cell createStyledInfoValueCell(String value, PdfFont regularFont) {
        return new Cell()
            .add(new Paragraph(value)
                .setFont(regularFont)
                .setFontSize(12)
                .setFontColor(SECONDARY_DARK))
            .setBackgroundColor(WHITE)
            .setBorder(new SolidBorder(LIGHT_GRAY, 1))
            .setPadding(12)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);
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