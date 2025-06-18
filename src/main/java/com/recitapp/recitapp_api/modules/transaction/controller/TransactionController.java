package com.recitapp.recitapp_api.modules.transaction.controller;

import com.recitapp.recitapp_api.annotation.RequireRole;
import com.recitapp.recitapp_api.modules.transaction.dto.*;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import com.recitapp.recitapp_api.modules.transaction.service.EnhancedRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final EnhancedRefundService enhancedRefundService;

    // RAPP113935-97: Register payment transaction
    @PostMapping
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<TransactionDTO> registerTransaction(@RequestBody TransactionDTO transactionDTO) {
        TransactionDTO savedTransaction = transactionService.registerTransaction(transactionDTO);
        return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);
    }

    // RAPP113935-98: Modify transaction status
    @PatchMapping("/{transactionId}/status")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<TransactionDTO> updateTransactionStatus(
            @PathVariable Long transactionId,
            @RequestBody TransactionStatusUpdateDTO statusUpdateDTO) {
        TransactionDTO updatedTransaction = transactionService.updateTransactionStatus(transactionId, statusUpdateDTO);
        return ResponseEntity.ok(updatedTransaction);
    }

    // RAPP113935-99: Query payment history
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getUserTransactionHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionDTO> transactions = transactionService.getUserTransactionHistory(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    // RAPP113935-100: Issue payment receipt
    @GetMapping("/{transactionId}/receipt")
    public ResponseEntity<TransactionReceiptDTO> generateTransactionReceipt(@PathVariable Long transactionId) {
        TransactionReceiptDTO receipt = transactionService.generateTransactionReceipt(transactionId);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/report")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<TransactionStatisticsDTO> generateTransactionStatistics(
            @RequestBody TransactionReportDTO reportDTO) {
        TransactionStatisticsDTO statistics = transactionService.generateTransactionStatistics(reportDTO);
        return ResponseEntity.ok(statistics);
    }

    // RAPP113935-102: Register refund
    @PostMapping("/refund")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<TransactionDTO> processRefund(@RequestBody RefundRequestDTO refundRequest) {
        TransactionDTO refundTransaction = transactionService.processRefund(refundRequest);
        return ResponseEntity.ok(refundTransaction);
    }

    // Enhanced refund with MercadoPago integration and wallet fallback
    @PostMapping("/refund/enhanced")
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<EnhancedRefundResponseDTO> processEnhancedRefund(@RequestBody EnhancedRefundRequestDTO refundRequest) {
        EnhancedRefundResponseDTO refundResponse = enhancedRefundService.processEnhancedRefund(refundRequest);
        return ResponseEntity.ok(refundResponse);
    }

    // Get transaction by ID
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long transactionId) {
        TransactionDTO transaction = transactionService.getTransactionById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    // Get all transactions
    @GetMapping
    @RequireRole({"ADMIN", "REGISTRADOR_EVENTO"})
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionDTO> transactions = transactionService.getAllTransactions(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
}