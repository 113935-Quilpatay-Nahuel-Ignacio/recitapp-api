package com.recitapp.recitapp_api.modules.transaction.controller;

import com.recitapp.recitapp_api.modules.transaction.dto.WalletTransactionDTO;
import com.recitapp.recitapp_api.modules.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final TransactionService transactionService;

    // RAPP113935-104: Register balance in virtual wallet
    @PostMapping("/transaction")
    public ResponseEntity<Void> updateWalletBalance(@RequestBody WalletTransactionDTO walletTransactionDTO) {
        transactionService.updateWalletBalance(walletTransactionDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<BigDecimal> getUserWalletBalance(@PathVariable Long userId) {
        BigDecimal balance = transactionService.getUserWalletBalance(userId);
        return ResponseEntity.ok(balance);
    }
}