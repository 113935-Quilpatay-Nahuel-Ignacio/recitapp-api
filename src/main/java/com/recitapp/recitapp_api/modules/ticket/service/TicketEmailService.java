package com.recitapp.recitapp_api.modules.ticket.service;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;

public interface TicketEmailService {
    void sendTicketByEmail(TicketDTO ticket, String recipientEmail);
    void sendTicketWithAttachment(TicketDTO ticket, String recipientEmail, byte[] pdfAttachment);
} 