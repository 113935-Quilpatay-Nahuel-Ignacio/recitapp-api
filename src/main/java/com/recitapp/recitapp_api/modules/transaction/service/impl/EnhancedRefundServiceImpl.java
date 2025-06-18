package com.recitapp.recitapp_api.modules.transaction.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.payment.dto.RefundResponseDTO;
import com.recitapp.recitapp_api.modules.payment.service.MercadoPagoRefundService;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketStatusRepository;
import com.recitapp.recitapp_api.modules.transaction.dto.EnhancedRefundRequestDTO;
import com.recitapp.recitapp_api.modules.transaction.dto.EnhancedRefundResponseDTO;
import com.recitapp.recitapp_api.modules.transaction.dto.WalletTransactionDTO;
import com.recitapp.recitapp_api.modules.transaction.entity.Transaction;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionDetail;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionStatus;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionDetailRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionStatusRepository;
import com.recitapp.recitapp_api.modules.transaction.service.EnhancedRefundService;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedRefundServiceImpl implements EnhancedRefundService {

    private final TransactionRepository transactionRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final TransactionStatusRepository transactionStatusRepository;
    private final TicketRepository ticketRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final UserRepository userRepository;
    private final MercadoPagoRefundService mercadoPagoRefundService;
    private final TransactionService transactionService;

    @Override
    @Transactional
    public EnhancedRefundResponseDTO processEnhancedRefund(EnhancedRefundRequestDTO refundRequest) {
        log.info("Processing enhanced refund for transaction ID: {}", refundRequest.getTransactionId());

        // 1. Validate and get original transaction
        Transaction originalTransaction = transactionRepository.findById(refundRequest.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + refundRequest.getTransactionId()));

        if (!"COMPLETADA".equals(originalTransaction.getStatus().getName())) {
            throw new RecitappException("Cannot refund a transaction with status: " + originalTransaction.getStatus().getName());
        }

        // 2. Get transaction details and calculate refund amount
        List<TransactionDetail> originalDetails = transactionDetailRepository.findByTransactionId(refundRequest.getTransactionId());
        List<TransactionDetail> detailsToRefund = determineDetailsToRefund(originalDetails, refundRequest);
        BigDecimal refundAmount = calculateRefundAmount(detailsToRefund);

        // 3. Attempt MercadoPago refund first
        String mercadoPagoPaymentId = extractMercadoPagoPaymentId(originalTransaction, refundRequest);
        RefundResponseDTO mercadoPagoResult = null;
        
        if (mercadoPagoPaymentId != null) {
            log.info("Attempting MercadoPago refund for payment ID: {}", mercadoPagoPaymentId);
            mercadoPagoResult = mercadoPagoRefundService.processRefund(
                    mercadoPagoPaymentId, 
                    refundAmount, 
                    refundRequest.getReason()
            );
        }

        // 4. Create refund transaction
        Transaction refundTransaction = createRefundTransaction(originalTransaction, refundAmount, refundRequest);
        Transaction savedRefundTransaction = transactionRepository.save(refundTransaction);

        // 5. Process based on MercadoPago result
        if (mercadoPagoResult != null && mercadoPagoResult.isSuccess()) {
            // MercadoPago refund successful
            log.info("MercadoPago refund successful for transaction {}", refundRequest.getTransactionId());
            
            updateTicketStatuses(detailsToRefund);
            createRefundDetails(savedRefundTransaction, detailsToRefund);
            
            return EnhancedRefundResponseDTO.mercadoPagoSuccess(
                    savedRefundTransaction.getId(),
                    originalTransaction.getId(),
                    refundAmount,
                    mercadoPagoResult.getRefundId()
            );
            
        } else if (Boolean.TRUE.equals(refundRequest.getAllowWalletFallback())) {
            // MercadoPago failed, use wallet fallback
            log.warn("MercadoPago refund failed, using wallet fallback for transaction {}", refundRequest.getTransactionId());
            
            // Update transaction description to reflect wallet fallback usage
            updateTransactionDescriptionForWalletFallback(savedRefundTransaction, refundRequest.getReason());
            
            BigDecimal newWalletBalance = processWalletFallback(originalTransaction.getUser(), refundAmount);
            updateTicketStatuses(detailsToRefund);
            createRefundDetails(savedRefundTransaction, detailsToRefund);
            
            String errorMessage = mercadoPagoResult != null ? mercadoPagoResult.getErrorMessage() : "MercadoPago payment ID not found";
            
            return EnhancedRefundResponseDTO.walletFallback(
                    savedRefundTransaction.getId(),
                    originalTransaction.getId(),
                    refundAmount,
                    newWalletBalance,
                    errorMessage
            );
            
        } else {
            // Both MercadoPago and wallet fallback failed/not allowed
            log.error("Refund failed for transaction {} - MercadoPago failed and wallet fallback not allowed", refundRequest.getTransactionId());
            
            // Delete the created refund transaction since it failed
            transactionRepository.delete(savedRefundTransaction);
            
            String errorMessage = mercadoPagoResult != null ? mercadoPagoResult.getErrorMessage() : "MercadoPago payment ID not found";
            
            return EnhancedRefundResponseDTO.failure(
                    originalTransaction.getId(),
                    refundAmount,
                    errorMessage
            );
        }
    }

    @Override
    public boolean canRefundThroughMercadoPago(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + transactionId));

        String mercadoPagoPaymentId = extractMercadoPagoPaymentId(transaction, null);
        
        if (mercadoPagoPaymentId == null) {
            return false;
        }

        return mercadoPagoRefundService.canRefund(mercadoPagoPaymentId);
    }

    private List<TransactionDetail> determineDetailsToRefund(List<TransactionDetail> originalDetails, EnhancedRefundRequestDTO refundRequest) {
        if (Boolean.TRUE.equals(refundRequest.getFullRefund())) {
            return originalDetails;
        } else {
            if (refundRequest.getTicketIds() == null || refundRequest.getTicketIds().isEmpty()) {
                throw new RecitappException("Ticket IDs are required for partial refund");
            }

            List<TransactionDetail> detailsToRefund = originalDetails.stream()
                    .filter(detail -> refundRequest.getTicketIds().contains(detail.getTicket().getId()))
                    .collect(Collectors.toList());

            if (detailsToRefund.isEmpty()) {
                throw new RecitappException("None of the provided ticket IDs match the original transaction");
            }

            return detailsToRefund;
        }
    }

    private BigDecimal calculateRefundAmount(List<TransactionDetail> detailsToRefund) {
        return detailsToRefund.stream()
                .map(TransactionDetail::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String extractMercadoPagoPaymentId(Transaction transaction, EnhancedRefundRequestDTO refundRequest) {
        // First check if explicitly provided in request
        if (refundRequest != null && refundRequest.getMercadoPagoPaymentId() != null) {
            return refundRequest.getMercadoPagoPaymentId();
        }

        // Extract from external reference
        String externalRef = transaction.getExternalReference();
        if (externalRef != null) {
            // Check if it contains the payment ID in the format "ORIGINAL_REF|PAYMENT_ID"
            if (externalRef.contains("|")) {
                String[] parts = externalRef.split("\\|");
                if (parts.length == 2) {
                    String potentialPaymentId = parts[1];
                    // Validate that it's a number (MercadoPago payment IDs are numeric)
                    if (potentialPaymentId.matches("\\d+")) {
                        log.info("Extracted MercadoPago payment ID from external reference: {}", potentialPaymentId);
                        return potentialPaymentId;
                    }
                }
            }
            
            // Fallback: check if the entire external reference is a number
            if (externalRef.matches("\\d+")) {
                return externalRef;
            }
        }

        // Could not determine MercadoPago payment ID
        log.warn("Could not extract MercadoPago payment ID from transaction {}. External reference: {}", 
                transaction.getId(), externalRef);
        return null;
    }

    private Transaction createRefundTransaction(Transaction originalTransaction, BigDecimal refundAmount, EnhancedRefundRequestDTO refundRequest) {
        TransactionStatus refundedStatus = transactionStatusRepository.findByName("REEMBOLSADA")
                .orElseThrow(() -> new EntityNotFoundException("Transaction status 'REEMBOLSADA' not found"));

        Transaction refundTransaction = new Transaction();
        refundTransaction.setUser(originalTransaction.getUser());
        refundTransaction.setPaymentMethod(originalTransaction.getPaymentMethod());
        refundTransaction.setStatus(refundedStatus);
        refundTransaction.setTotalAmount(refundAmount);
        refundTransaction.setExternalReference("REFUND-" + UUID.randomUUID().toString().substring(0, 8));
        refundTransaction.setTransactionDate(LocalDateTime.now());
        refundTransaction.setDescription(refundRequest.getReason() != null ?
                refundRequest.getReason() : "Enhanced refund for transaction " + originalTransaction.getId());
        refundTransaction.setIsRefund(true);
        refundTransaction.setOriginalTransaction(originalTransaction);

        return refundTransaction;
    }

    private void updateTransactionDescriptionForWalletFallback(Transaction refundTransaction, String originalReason) {
        // Clean the description to remove any reference to MercadoPago success
        String cleanedReason = originalReason;
        if (cleanedReason != null) {
            // Remove references to MercadoPago processing
            cleanedReason = cleanedReason.replaceAll("(?i)\\s*-\\s*procesado\\s+con\\s+sistema\\s+mejorado\\s+mercadopago.*", "");
            cleanedReason = cleanedReason.replaceAll("(?i)\\s*-\\s*processed\\s+with\\s+improved\\s+mercadopago.*", "");
        }
        
        // Set appropriate description for wallet fallback
        String fallbackDescription;
        if (cleanedReason != null && !cleanedReason.trim().isEmpty()) {
            fallbackDescription = cleanedReason.trim() + " - Procesado como crédito en billetera virtual";
        } else {
            fallbackDescription = "Reembolso procesado como crédito en billetera virtual";
        }
        
        refundTransaction.setDescription(fallbackDescription);
        transactionRepository.save(refundTransaction);
        log.info("Updated transaction {} description for wallet fallback: {}", 
                refundTransaction.getId(), fallbackDescription);
    }

    private BigDecimal processWalletFallback(User user, BigDecimal refundAmount) {
        WalletTransactionDTO walletTransaction = new WalletTransactionDTO();
        walletTransaction.setUserId(user.getId());
        walletTransaction.setOperation("ADD");
        walletTransaction.setAmount(refundAmount);
        walletTransaction.setCurrency("ARS");
        walletTransaction.setDescription("Reembolso acreditado en billetera virtual (MercadoPago no disponible)");

        transactionService.updateWalletBalance(walletTransaction);

        return transactionService.getUserWalletBalance(user.getId());
    }

    private void updateTicketStatuses(List<TransactionDetail> detailsToRefund) {
        TicketStatus canceledStatus = ticketStatusRepository.findByName("CANCELADA")
                .orElseThrow(() -> new EntityNotFoundException("Ticket status 'CANCELADA' not found"));

        for (TransactionDetail detail : detailsToRefund) {
            Ticket ticket = detail.getTicket();
            ticket.setStatus(canceledStatus);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
    }

    private void createRefundDetails(Transaction refundTransaction, List<TransactionDetail> detailsToRefund) {
        List<TransactionDetail> refundDetails = new ArrayList<>();

        for (TransactionDetail originalDetail : detailsToRefund) {
            TransactionDetail refundDetail = new TransactionDetail();
            TransactionDetail.TransactionDetailId detailId = new TransactionDetail.TransactionDetailId();
            detailId.setTransactionId(refundTransaction.getId());
            detailId.setTicketId(originalDetail.getTicket().getId());

            refundDetail.setId(detailId);
            refundDetail.setTransaction(refundTransaction);
            refundDetail.setTicket(originalDetail.getTicket());
            refundDetail.setUnitPrice(originalDetail.getUnitPrice());

            refundDetails.add(refundDetail);
        }

        transactionDetailRepository.saveAll(refundDetails);
    }
} 