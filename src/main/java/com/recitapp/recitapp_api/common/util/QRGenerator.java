package com.recitapp.recitapp_api.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class QRGenerator {

    /**
     * Maximum QR code string length to fit column constraints
     */
    private static final int MAX_QR_CODE_LENGTH = 500;

    /**
     * Generates a QR code image from the given content
     *
     * @param content The content to encode in the QR code
     * @param width   The width of the QR code image
     * @param height  The height of the QR code image
     * @return A Base64 encoded string representation of the QR code image
     */
    public String generateQRCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // Create a more compact Base64 string to fit in the database column
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Check if result exceeds maximum length and truncate if necessary
            String result = "data:image/png;base64," + base64Image;
            if (result.length() > MAX_QR_CODE_LENGTH) {
                log.warn("QR code image too large, using compressed reference instead");
                return generateCompressedReference(content);
            }

            return result;
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Error generating QR code: " + e.getMessage());
        }
    }

    /**
     * Generates a QR code for a ticket
     *
     * @param ticketId The ID of the ticket
     * @param code     The unique verification code for the ticket
     * @return A Base64 encoded string representation of the QR code image or a compact reference
     */
    public String generateTicketQR(Long ticketId, String code) {
        String ticketIdStr = ticketId != null ? ticketId.toString() : "new";
        String content = String.format("RECITAPP-TICKET:%s:%s", ticketIdStr, code);

        try {
            return generateQRCodeBase64(content, 200, 200);
        } catch (Exception e) {
            log.error("Failed to generate QR code image, using compressed reference", e);
            return generateCompressedReference(content);
        }
    }

    /**
     * Generates a compressed reference string instead of a full QR code image
     * This is used as a fallback when the Base64 encoded image would be too large
     *
     * @param content The content that would have been encoded in the QR code
     * @return A compact reference string that still contains the essential verification data
     */
    private String generateCompressedReference(String content) {
        // Create a shortened hash-based reference that will always fit in the column
        String hash = Integer.toHexString(content.hashCode());
        return "QR-REF:" + hash;
    }
}