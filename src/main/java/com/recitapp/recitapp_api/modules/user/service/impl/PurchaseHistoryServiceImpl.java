package com.recitapp.recitapp_api.modules.user.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.transaction.entity.Transaction;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionDetail;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionRepository;
import com.recitapp.recitapp_api.modules.user.dto.PurchaseHistoryDTO;
import com.recitapp.recitapp_api.modules.user.dto.TicketPurchaseDTO;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.service.PurchaseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryServiceImpl implements PurchaseHistoryService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<PurchaseHistoryDTO> getUserPurchaseHistory(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RecitappException("Usuario no encontrado con ID: " + userId);
        }

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

        return transactions.stream()
                .filter(t -> !t.getIsRefund()) // Excluir reembolsos para mostrar solo compras
                .map(this::mapToPurchaseHistoryDTO)
                .collect(Collectors.toList());
    }

    private PurchaseHistoryDTO mapToPurchaseHistoryDTO(Transaction transaction) {
        List<TicketPurchaseDTO> ticketDTOs = new ArrayList<>();

        // Aquí necesitaríamos un repositorio o método para obtener los detalles de transacción
        // Por ahora dejaré esto como pseudocódigo comentado
        /*
        List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(transaction.getId());

        ticketDTOs = details.stream()
                .map(detail -> TicketPurchaseDTO.builder()
                        .ticketId(detail.getTicket().getId())
                        .eventName(detail.getTicket().getEvent().getName())
                        .artistName(detail.getTicket().getEvent().getArtistPrincipal().getName())
                        .venueName(detail.getTicket().getEvent().getVenue().getName())
                        .section(detail.getTicket().getSection().getName())
                        .eventDate(detail.getTicket().getEvent().getStartDateTime())
                        .price(detail.getUnitPrice())
                        .ticketStatus(detail.getTicket().getStatus().getName())
                        .qrCode(detail.getTicket().getQrCode())
                        .build())
                .collect(Collectors.toList());
        */

        return PurchaseHistoryDTO.builder()
                .transactionId(transaction.getId())
                .purchaseDate(transaction.getTransactionDate())
                .totalAmount(transaction.getTotalAmount())
                .paymentMethod(transaction.getPaymentMethod().getName())
                .transactionStatus(transaction.getStatus().getName())
                .tickets(ticketDTOs)
                .build();
    }
}