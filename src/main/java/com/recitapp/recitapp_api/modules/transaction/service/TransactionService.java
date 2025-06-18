package com.recitapp.recitapp_api.modules.transaction.service;

import com.recitapp.recitapp_api.modules.transaction.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    // RAPP113935-97: Register payment transaction
    TransactionDTO registerTransaction(TransactionDTO transactionDTO);

    // RAPP113935-98: Modify transaction status
    TransactionDTO updateTransactionStatus(Long transactionId, TransactionStatusUpdateDTO statusUpdateDTO);

    // RAPP113935-99: Query payment history
    List<TransactionDTO> getUserTransactionHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // RAPP113935-100: Issue payment receipt
    TransactionReceiptDTO generateTransactionReceipt(Long transactionId);

    // RAPP113935-102: Register refund
    TransactionDTO processRefund(RefundRequestDTO refundRequest);

    // RAPP113935-103: Update available payment methods
    PaymentMethodDTO createPaymentMethod(PaymentMethodDTO paymentMethodDTO);
    PaymentMethodDTO updatePaymentMethod(Long paymentMethodId, PaymentMethodDTO paymentMethodDTO);
    void deletePaymentMethod(Long paymentMethodId);
    List<PaymentMethodDTO> getActivePaymentMethods();
    List<PaymentMethodDTO> getPaymentMethods(Boolean includeInactive);

    // RAPP113935-104: Register balance in virtual wallet
    void updateWalletBalance(WalletTransactionDTO walletTransactionDTO);
    BigDecimal getUserWalletBalance(Long userId);

    // Additional utility methods
    TransactionDTO getTransactionById(Long transactionId);
    List<TransactionDTO> getAllTransactions(LocalDateTime startDate, LocalDateTime endDate);

    TransactionStatisticsDTO generateTransactionStatistics(TransactionReportDTO reportDTO);

    // MercadoPago webhook integration methods
    TransactionDTO findByExternalReference(String externalReference);
    void updateExternalReference(Long transactionId, String newExternalReference);
}