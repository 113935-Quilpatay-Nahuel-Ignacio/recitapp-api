package com.recitapp.recitapp_api.modules.user.service.impl;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.transaction.entity.Transaction;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionDetail;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionDetailRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionRepository;
import com.recitapp.recitapp_api.modules.user.dto.PurchaseHistoryDTO;
import com.recitapp.recitapp_api.modules.user.dto.TicketPurchaseDTO;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import com.recitapp.recitapp_api.modules.user.service.PurchaseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryServiceImpl implements PurchaseHistoryService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionDetailRepository transactionDetailRepository;

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
        List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(transaction.getId());

        List<TicketPurchaseDTO> ticketDTOs = details.stream()
                .map(detail -> {
                    var ticket = detail.getTicket();
                    var event = ticket.getEvent();

                    return TicketPurchaseDTO.builder()
                            .ticketId(ticket.getId())
                            .eventName(event.getName())
                            .artistName(event.getMainArtist() != null ? event.getMainArtist().getName() : "N/A")
                            .venueName(event.getVenue().getName())
                            .section(ticket.getSection().getName())
                            .eventDate(event.getStartDateTime())
                            .price(detail.getUnitPrice())
                            .ticketStatus(ticket.getStatus().getName())
                            .qrCode(ticket.getQrCode())
                            .build();
                })
                .collect(Collectors.toList());

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