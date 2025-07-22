package com.recitapp.recitapp_api.modules.transaction.controller;

import com.recitapp.recitapp_api.modules.transaction.dto.PaymentMethodDTO;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final TransactionService transactionService;

    // RAPP113935-103: Update available payment methods
    @GetMapping
    public ResponseEntity<List<PaymentMethodDTO>> getPaymentMethods(
            @RequestParam(defaultValue = "false") Boolean includeInactive) {
        List<PaymentMethodDTO> paymentMethods = transactionService.getPaymentMethods(includeInactive);
        return ResponseEntity.ok(paymentMethods);
    }

    @PostMapping
    public ResponseEntity<PaymentMethodDTO> createPaymentMethod(@RequestBody PaymentMethodDTO paymentMethodDTO) {
        PaymentMethodDTO createdMethod = transactionService.createPaymentMethod(paymentMethodDTO);
        return new ResponseEntity<>(createdMethod, HttpStatus.CREATED);
    }

    @PutMapping("/{paymentMethodId}")
    public ResponseEntity<PaymentMethodDTO> updatePaymentMethod(
            @PathVariable Long paymentMethodId,
            @RequestBody PaymentMethodDTO paymentMethodDTO) {
        PaymentMethodDTO updatedMethod = transactionService.updatePaymentMethod(paymentMethodId, paymentMethodDTO);
        return ResponseEntity.ok(updatedMethod);
    }

    @DeleteMapping("/{paymentMethodId}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long paymentMethodId) {
        transactionService.deletePaymentMethod(paymentMethodId);
        return ResponseEntity.noContent().build();
    }
}