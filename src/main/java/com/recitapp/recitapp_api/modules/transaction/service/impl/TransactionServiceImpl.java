package com.recitapp.recitapp_api.modules.transaction.service.impl;

import com.recitapp.recitapp_api.common.exception.EntityNotFoundException;
import com.recitapp.recitapp_api.common.exception.RecitappException;
import com.recitapp.recitapp_api.modules.ticket.entity.Ticket;
import com.recitapp.recitapp_api.modules.ticket.entity.TicketStatus;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketRepository;
import com.recitapp.recitapp_api.modules.ticket.repository.TicketStatusRepository;
import com.recitapp.recitapp_api.modules.transaction.dto.*;
import com.recitapp.recitapp_api.modules.transaction.entity.PaymentMethod;
import com.recitapp.recitapp_api.modules.transaction.entity.Transaction;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionDetail;
import com.recitapp.recitapp_api.modules.transaction.entity.TransactionStatus;
import com.recitapp.recitapp_api.modules.transaction.repository.PaymentMethodRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionDetailRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionRepository;
import com.recitapp.recitapp_api.modules.transaction.repository.TransactionStatusRepository;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import com.recitapp.recitapp_api.modules.user.entity.User;
import com.recitapp.recitapp_api.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionDetailRepository transactionDetailRepository;
    private final TransactionStatusRepository transactionStatusRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketStatusRepository ticketStatusRepository;

    @Override
    @Transactional
    public TransactionDTO registerTransaction(TransactionDTO transactionDTO) {
        log.info("Registering new transaction for user ID: {}", transactionDTO.getUserId());

        User user = userRepository.findById(transactionDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + transactionDTO.getUserId()));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(transactionDTO.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with ID: " + transactionDTO.getPaymentMethodId()));

        TransactionStatus status = transactionStatusRepository.findByName("INICIADA")
                .orElseThrow(() -> new EntityNotFoundException("Transaction status 'INICIADA' not found"));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setStatus(status);
        transaction.setTotalAmount(transactionDTO.getTotalAmount());
        transaction.setExternalReference(transactionDTO.getExternalReference());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setIsRefund(false);

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (transactionDTO.getDetails() != null && !transactionDTO.getDetails().isEmpty()) {
            List<TransactionDetail> details = new ArrayList<>();

            for (TransactionDetailDTO detailDTO : transactionDTO.getDetails()) {
                Ticket ticket = ticketRepository.findById(detailDTO.getTicketId())
                        .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + detailDTO.getTicketId()));

                TransactionDetail detail = new TransactionDetail();
                TransactionDetail.TransactionDetailId detailId = new TransactionDetail.TransactionDetailId();
                detailId.setTransactionId(savedTransaction.getId());
                detailId.setTicketId(ticket.getId());

                detail.setId(detailId);
                detail.setTransaction(savedTransaction);
                detail.setTicket(ticket);
                detail.setUnitPrice(detailDTO.getUnitPrice());

                details.add(detail);
            }

            transactionDetailRepository.saveAll(details);
        }

        return mapToDTO(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO updateTransactionStatus(Long transactionId, TransactionStatusUpdateDTO statusUpdateDTO) {
        log.info("Updating transaction status for transaction ID: {}, new status: {}",
                transactionId, statusUpdateDTO.getStatusName());

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + transactionId));

        TransactionStatus newStatus = transactionStatusRepository.findByName(statusUpdateDTO.getStatusName())
                .orElseThrow(() -> new EntityNotFoundException("Transaction status not found: " + statusUpdateDTO.getStatusName()));

        transaction.setStatus(newStatus);

        if (statusUpdateDTO.getExternalReference() != null) {
            transaction.setExternalReference(statusUpdateDTO.getExternalReference());
        }

        transaction.setUpdatedAt(LocalDateTime.now());

        // If status is COMPLETADA, update the tickets statuses to VENDIDA if needed
        if ("COMPLETADA".equals(statusUpdateDTO.getStatusName())) {
            List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(transactionId);
            if (!details.isEmpty()) {
                TicketStatus soldStatus = ticketStatusRepository.findByName("VENDIDA")
                        .orElseThrow(() -> new EntityNotFoundException("Ticket status 'VENDIDA' not found"));

                for (TransactionDetail detail : details) {
                    Ticket ticket = detail.getTicket();
                    if (!"VENDIDA".equals(ticket.getStatus().getName()) && !"USADA".equals(ticket.getStatus().getName())) {
                        ticket.setStatus(soldStatus);
                        ticket.setUpdatedAt(LocalDateTime.now());
                        ticketRepository.save(ticket);
                    }
                }
            }
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return mapToDTO(updatedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getUserTransactionHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting transaction history for user ID: {}, startDate: {}, endDate: {}",
                userId, startDate, endDate);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }

        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    userId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
        }

        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReceiptDTO generateTransactionReceipt(Long transactionId) {
        log.info("Generating receipt for transaction ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + transactionId));

        // Validating transaction status
        if (!"COMPLETADA".equals(transaction.getStatus().getName()) &&
                !"REEMBOLSADA".equals(transaction.getStatus().getName())) {
            throw new RecitappException("Cannot generate receipt for transaction with status: " +
                    transaction.getStatus().getName());
        }

        List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(transactionId);

        List<ReceiptItemDTO> items = new ArrayList<>();

        for (TransactionDetail detail : details) {
            Ticket ticket = detail.getTicket();
            String itemDescription = "Entrada para " + ticket.getEvent().getName() +
                    " - Sección: " + ticket.getSection().getName();

            ReceiptItemDTO item = ReceiptItemDTO.builder()
                    .itemDescription(itemDescription)
                    .unitPrice(detail.getUnitPrice())
                    .quantity(1)
                    .subtotal(detail.getUnitPrice())
                    .build();

            items.add(item);
        }

        User user = transaction.getUser();

        // Generate receipt number
        String receiptNumber = "REC-" + transaction.getId() + "-" +
                DateTimeFormatter.ofPattern("yyyyMMdd").format(transaction.getTransactionDate());

        return TransactionReceiptDTO.builder()
                .transactionId(transaction.getId())
                .receiptNumber(receiptNumber)
                .issueDate(LocalDateTime.now())
                .userFullName(user.getFirstName() + " " + user.getLastName())
                .userDni(user.getDni())
                .totalAmount(transaction.getTotalAmount())
                .paymentMethod(transaction.getPaymentMethod().getName())
                .items(items)
                .isRefund(transaction.getIsRefund())
                .build();
    }

    @Override
    @Transactional
    public TransactionDTO processRefund(RefundRequestDTO refundRequest) {
        log.info("Processing refund for transaction ID: {}", refundRequest.getTransactionId());

        Transaction originalTransaction = transactionRepository.findById(refundRequest.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + refundRequest.getTransactionId()));

        // Validate transaction status
        if (!"COMPLETADA".equals(originalTransaction.getStatus().getName())) {
            throw new RecitappException("Cannot refund a transaction with status: " +
                    originalTransaction.getStatus().getName());
        }

        // Get transaction details
        List<TransactionDetail> originalDetails = transactionDetailRepository.findByTransactionId(refundRequest.getTransactionId());

        // Determine which tickets to refund
        List<TransactionDetail> detailsToRefund;

        if (Boolean.TRUE.equals(refundRequest.getFullRefund())) {
            detailsToRefund = originalDetails;
        } else {
            if (refundRequest.getTicketIds() == null || refundRequest.getTicketIds().isEmpty()) {
                throw new RecitappException("Ticket IDs are required for partial refund");
            }

            detailsToRefund = originalDetails.stream()
                    .filter(detail -> refundRequest.getTicketIds().contains(detail.getTicket().getId()))
                    .collect(Collectors.toList());

            if (detailsToRefund.isEmpty()) {
                throw new RecitappException("None of the provided ticket IDs match the original transaction");
            }
        }

        // Calculate refund amount
        BigDecimal refundAmount = detailsToRefund.stream()
                .map(TransactionDetail::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create refund transaction
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
                refundRequest.getReason() : "Refund for transaction " + originalTransaction.getId());
        refundTransaction.setIsRefund(true);
        refundTransaction.setOriginalTransaction(originalTransaction);

        Transaction savedRefundTransaction = transactionRepository.save(refundTransaction);

        // Create refund transaction details and update ticket statuses
        List<TransactionDetail> refundDetails = new ArrayList<>();
        TicketStatus canceledStatus = ticketStatusRepository.findByName("CANCELADA")
                .orElseThrow(() -> new EntityNotFoundException("Ticket status 'CANCELADA' not found"));

        for (TransactionDetail originalDetail : detailsToRefund) {
            // Create refund detail
            TransactionDetail refundDetail = new TransactionDetail();
            TransactionDetail.TransactionDetailId detailId = new TransactionDetail.TransactionDetailId();
            detailId.setTransactionId(savedRefundTransaction.getId());
            detailId.setTicketId(originalDetail.getTicket().getId());

            refundDetail.setId(detailId);
            refundDetail.setTransaction(savedRefundTransaction);
            refundDetail.setTicket(originalDetail.getTicket());
            refundDetail.setUnitPrice(originalDetail.getUnitPrice());

            refundDetails.add(refundDetail);

            // Update ticket status to CANCELADA
            Ticket ticket = originalDetail.getTicket();
            ticket.setStatus(canceledStatus);
            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }

        transactionDetailRepository.saveAll(refundDetails);

        // Update user's wallet balance with refund amount
        User user = originalTransaction.getUser();
        Double currentWalletBalance = user.getWalletBalance() != null ? user.getWalletBalance() : 0.0;
        user.setWalletBalance(currentWalletBalance + refundAmount.doubleValue());
        userRepository.save(user);

        log.info("Added refund amount {} to user {} wallet balance. New balance: {}", 
                refundAmount, user.getId(), user.getWalletBalance());

        return mapToDTO(savedRefundTransaction);
    }

    @Override
    @Transactional
    public PaymentMethodDTO createPaymentMethod(PaymentMethodDTO paymentMethodDTO) {
        log.info("Creating new payment method: {}", paymentMethodDTO.getName());

        // Check if a payment method with the same name already exists
        if (paymentMethodRepository.existsByName(paymentMethodDTO.getName())) {
            throw new RecitappException("Payment method with name '" + paymentMethodDTO.getName() + "' already exists");
        }

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setName(paymentMethodDTO.getName());
        paymentMethod.setDescription(paymentMethodDTO.getDescription());
        paymentMethod.setActive(paymentMethodDTO.getActive() != null ? paymentMethodDTO.getActive() : true);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);

        return mapToPaymentMethodDTO(savedPaymentMethod);
    }

    @Override
    @Transactional
    public PaymentMethodDTO updatePaymentMethod(Long paymentMethodId, PaymentMethodDTO paymentMethodDTO) {
        log.info("Updating payment method with ID: {}", paymentMethodId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with ID: " + paymentMethodId));

        // Check if new name conflicts with existing payment method
        if (paymentMethodDTO.getName() != null &&
                !paymentMethod.getName().equals(paymentMethodDTO.getName()) &&
                paymentMethodRepository.existsByName(paymentMethodDTO.getName())) {
            throw new RecitappException("Payment method with name '" + paymentMethodDTO.getName() + "' already exists");
        }

        if (paymentMethodDTO.getName() != null) {
            paymentMethod.setName(paymentMethodDTO.getName());
        }

        if (paymentMethodDTO.getDescription() != null) {
            paymentMethod.setDescription(paymentMethodDTO.getDescription());
        }

        if (paymentMethodDTO.getActive() != null) {
            paymentMethod.setActive(paymentMethodDTO.getActive());
        }

        PaymentMethod updatedPaymentMethod = paymentMethodRepository.save(paymentMethod);

        return mapToPaymentMethodDTO(updatedPaymentMethod);
    }

    @Override
    @Transactional
    public void deletePaymentMethod(Long paymentMethodId) {
        log.info("Deleting payment method with ID: {}", paymentMethodId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with ID: " + paymentMethodId));

        // Check if there are transactions associated with this payment method
        long transactionCount = transactionRepository.countByPaymentMethodId(paymentMethodId);

        if (transactionCount > 0) {
            // Instead of deleting, just deactivate
            paymentMethod.setActive(false);
            paymentMethodRepository.save(paymentMethod);
            log.info("Payment method ID {} has transactions, deactivated instead of deleted", paymentMethodId);
        } else {
            paymentMethodRepository.delete(paymentMethod);
            log.info("Payment method ID {} deleted successfully", paymentMethodId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getActivePaymentMethods() {
        log.info("Getting all active payment methods");

        List<PaymentMethod> paymentMethods = paymentMethodRepository.findByActiveTrue();

        return paymentMethods.stream()
                .map(this::mapToPaymentMethodDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getPaymentMethods(Boolean includeInactive) {
        log.info("Getting payment methods, includeInactive: {}", includeInactive);

        List<PaymentMethod> paymentMethods;
        if (includeInactive) {
            paymentMethods = paymentMethodRepository.findAll();
        } else {
            paymentMethods = paymentMethodRepository.findByActiveTrue();
        }

        return paymentMethods.stream()
                .map(this::mapToPaymentMethodDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateWalletBalance(WalletTransactionDTO walletTransactionDTO) {
        log.info("Updating wallet balance for user ID: {}, operation: {}, amount: {}",
                walletTransactionDTO.getUserId(), walletTransactionDTO.getOperation(), walletTransactionDTO.getAmount());

        User user = userRepository.findById(walletTransactionDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + walletTransactionDTO.getUserId()));

        Double currentBalance = user.getWalletBalance() != null ?
                user.getWalletBalance() : 0.0;

        Double newBalance;

        if ("ADD".equals(walletTransactionDTO.getOperation())) {
            newBalance = currentBalance + walletTransactionDTO.getAmount().doubleValue();
        } else if ("SUBTRACT".equals(walletTransactionDTO.getOperation())) {
            if (currentBalance < walletTransactionDTO.getAmount().doubleValue()) {
                throw new RecitappException("Insufficient wallet balance");
            }
            newBalance = currentBalance - walletTransactionDTO.getAmount().doubleValue();
        } else {
            throw new RecitappException("Invalid operation: " + walletTransactionDTO.getOperation());
        }

        user.setWalletBalance(newBalance);
        userRepository.save(user);

        // In a real implementation, would also log this wallet transaction in a dedicated table
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUserWalletBalance(Long userId) {
        log.info("Getting wallet balance for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        return user.getWalletBalance() != null ? BigDecimal.valueOf(user.getWalletBalance()) : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long transactionId) {
        log.info("Getting transaction with ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + transactionId));

        return mapToDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting all transactions between {} and {}", startDate, endDate);

        List<Transaction> transactions = transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(
                startDate, endDate);

        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionStatisticsDTO generateTransactionStatistics(TransactionReportDTO reportDTO) {
        log.info("Generating transaction statistics of type: {}", reportDTO.getReportType());

        List<Transaction> transactions = new ArrayList<>();
        TransactionStatisticsDTO.TransactionStatisticsDTOBuilder builder = TransactionStatisticsDTO.builder()
                .reportType(reportDTO.getReportType())
                .startDate(reportDTO.getStartDate())
                .endDate(reportDTO.getEndDate())
                .generatedDate(LocalDateTime.now());

        switch (reportDTO.getReportType()) {
            case "USER":
                if (reportDTO.getUserId() == null) {
                    throw new RecitappException("User ID is required for USER report type");
                }
                transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        reportDTO.getUserId(), reportDTO.getStartDate(), reportDTO.getEndDate());

                // Get user details if available
                userRepository.findById(reportDTO.getUserId()).ifPresent(user -> {
                    builder.userId(user.getId());
                    builder.userName(user.getFirstName() + " " + user.getLastName());
                });
                break;

            case "PAYMENT_METHOD":
                if (reportDTO.getPaymentMethodId() == null) {
                    throw new RecitappException("Payment method ID is required for PAYMENT_METHOD report type");
                }
                transactions = transactionRepository.findByPaymentMethodIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                        reportDTO.getPaymentMethodId(), reportDTO.getStartDate(), reportDTO.getEndDate());

                // Get payment method details if available
                paymentMethodRepository.findById(reportDTO.getPaymentMethodId()).ifPresent(paymentMethod -> {
                    builder.paymentMethodId(paymentMethod.getId());
                    builder.paymentMethodName(paymentMethod.getName());
                });
                break;

            case "STATUS":
                if (reportDTO.getStatusName() == null) {
                    throw new RecitappException("Status name is required for STATUS report type");
                }
                transactions = transactionRepository.findByStatusNameAndTransactionDateBetweenOrderByTransactionDateDesc(
                        reportDTO.getStatusName(), reportDTO.getStartDate(), reportDTO.getEndDate());
                builder.statusName(reportDTO.getStatusName());
                break;

            case "ALL":
            default:
                transactions = transactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(
                        reportDTO.getStartDate(), reportDTO.getEndDate());
                break;
        }

        // Calculate summary statistics
        int totalTransactions = transactions.size();

        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAmount = totalTransactions > 0 ?
                totalAmount.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        Optional<BigDecimal> maxAmount = transactions.stream()
                .map(Transaction::getTotalAmount)
                .max(BigDecimal::compareTo);

        Optional<BigDecimal> minAmount = transactions.stream()
                .map(Transaction::getTotalAmount)
                .min(BigDecimal::compareTo);

        builder.totalTransactions(totalTransactions)
                .totalAmount(totalAmount)
                .averageAmount(averageAmount)
                .maxAmount(maxAmount.orElse(BigDecimal.ZERO))
                .minAmount(minAmount.orElse(BigDecimal.ZERO));

        // Group by status
        Map<String, List<Transaction>> transactionsByStatus = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().getName()));

        Map<String, Integer> countByStatus = transactionsByStatus.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));

        Map<String, BigDecimal> amountByStatus = transactionsByStatus.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(Transaction::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ));

        builder.transactionsByStatus(countByStatus)
                .amountByStatus(amountByStatus);

        // Group by payment method
        Map<String, List<Transaction>> transactionsByPaymentMethod = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getPaymentMethod().getName()));

        Map<String, Integer> countByPaymentMethod = transactionsByPaymentMethod.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size()
                ));

        Map<String, BigDecimal> amountByPaymentMethod = transactionsByPaymentMethod.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(Transaction::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                ));

        builder.transactionsByPaymentMethod(countByPaymentMethod)
                .amountByPaymentMethod(amountByPaymentMethod);

        // Generate time-based analytics
        List<TransactionStatisticsDTO.TimeSegmentStatisticsDTO> timeSegments = generateTimeSegmentStatistics(
                transactions, reportDTO.getStartDate(), reportDTO.getEndDate());

        builder.timeSegmentStatistics(timeSegments);

        return builder.build();
    }

    /**
     * Generate time-based statistics by dividing the period into daily segments
     */
    private List<TransactionStatisticsDTO.TimeSegmentStatisticsDTO> generateTimeSegmentStatistics(
            List<Transaction> transactions, LocalDateTime startDate, LocalDateTime endDate) {

        // Always use daily segments (24 hours) for better visualization in bar charts
        int segmentSizeHours = 24;

        List<TransactionStatisticsDTO.TimeSegmentStatisticsDTO> timeSegments = new ArrayList<>();

        // Normalize start date to beginning of day for consistent daily segments
        LocalDateTime segmentStart = startDate.toLocalDate().atStartOfDay();
        LocalDateTime normalizedEndDate = endDate.toLocalDate().atTime(23, 59, 59);
        
        while (segmentStart.isBefore(normalizedEndDate)) {
            LocalDateTime segmentEnd = segmentStart.plusHours(segmentSizeHours);
            if (segmentEnd.isAfter(normalizedEndDate)) {
                segmentEnd = normalizedEndDate;
            }

            // Find transactions in this segment
            final LocalDateTime finalSegmentStart = segmentStart;
            final LocalDateTime finalSegmentEnd = segmentEnd;

            List<Transaction> segmentTransactions = transactions.stream()
                    .filter(t -> t.getTransactionDate() != null &&
                            !t.getTransactionDate().isBefore(finalSegmentStart) &&
                            t.getTransactionDate().isBefore(finalSegmentEnd))
                    .collect(Collectors.toList());

            int transactionCount = segmentTransactions.size();
            BigDecimal segmentTotal = segmentTransactions.stream()
                    .map(Transaction::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            timeSegments.add(TransactionStatisticsDTO.TimeSegmentStatisticsDTO.builder()
                    .segmentStart(segmentStart)
                    .segmentEnd(segmentEnd)
                    .transactionCount(transactionCount)
                    .totalAmount(segmentTotal)
                    .build());

            // Move to next segment
            segmentStart = segmentEnd;
        }

        return timeSegments;
    }


    // Helper methods

    private TransactionDTO mapToDTO(Transaction transaction) {
        List<TransactionDetailDTO> detailDTOs = new ArrayList<>();

        List<TransactionDetail> details = transactionDetailRepository.findByTransactionId(transaction.getId());
        for (TransactionDetail detail : details) {
            Ticket ticket = detail.getTicket();
            boolean isRefunded = "CANCELADA".equals(ticket.getStatus().getName());
            
            TransactionDetailDTO detailDTO = TransactionDetailDTO.builder()
                    .ticketId(ticket.getId())
                    .ticketCode(ticket.getIdentificationCode())
                    .eventName(ticket.getEvent().getName())
                    .unitPrice(detail.getUnitPrice())
                    .ticketStatus(ticket.getStatus().getName())
                    .isRefunded(isRefunded)
                    .build();

            detailDTOs.add(detailDTO);
        }

        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName())
                .paymentMethodId(transaction.getPaymentMethod().getId())
                .paymentMethodName(transaction.getPaymentMethod().getName())
                .totalAmount(transaction.getTotalAmount())
                .statusName(transaction.getStatus().getName())
                .externalReference(transaction.getExternalReference())
                .transactionDate(transaction.getTransactionDate())
                .details(detailDTOs)
                .description(transaction.getDescription())
                .isRefund(transaction.getIsRefund())
                .originalTransactionId(transaction.getOriginalTransaction() != null ?
                        transaction.getOriginalTransaction().getId() : null)
                .build();
    }

    private PaymentMethodDTO mapToPaymentMethodDTO(PaymentMethod paymentMethod) {
        return PaymentMethodDTO.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .description(paymentMethod.getDescription())
                .active(paymentMethod.getActive())
                .build();
    }

    // MercadoPago webhook integration methods

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO findByExternalReference(String externalReference) {
        log.info("Finding transaction by external reference: {}", externalReference);
        
        Optional<Transaction> transaction = transactionRepository.findByExternalReference(externalReference);
        
        if (transaction.isPresent()) {
            log.info("Found transaction with ID: {} for external reference: {}", 
                    transaction.get().getId(), externalReference);
            return mapToDTO(transaction.get());
        } else {
            log.warn("No transaction found for external reference: {}", externalReference);
            return null;
        }
    }

    @Override
    @Transactional
    public void updateExternalReference(Long transactionId, String newExternalReference) {
        log.info("Updating external reference for transaction ID: {} to: {}", transactionId, newExternalReference);
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with ID: " + transactionId));
        
        String oldExternalReference = transaction.getExternalReference();
        transaction.setExternalReference(newExternalReference);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        transactionRepository.save(transaction);
        
        log.info("Successfully updated external reference for transaction ID: {} from '{}' to '{}'", 
                transactionId, oldExternalReference, newExternalReference);
    }
}