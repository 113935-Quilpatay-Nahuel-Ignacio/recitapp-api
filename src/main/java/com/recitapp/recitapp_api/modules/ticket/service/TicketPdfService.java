package com.recitapp.recitapp_api.modules.ticket.service;

import com.recitapp.recitapp_api.modules.ticket.dto.TicketDTO;

public interface TicketPdfService {
    byte[] generateTicketPdf(TicketDTO ticket);
    String saveTicketPdf(TicketDTO ticket);
} 