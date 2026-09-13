package com.re.hackathong.transactionservice.controller;

import com.re.hackathong.transactionservice.dto.TransferRequest;
import com.re.hackathong.transactionservice.dto.TransferResponse;
import com.re.hackathong.transactionservice.model.Transaction;
import com.re.hackathong.transactionservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Value("${server.port:8083}")
    private String port;

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/info")
    public Map<String, String> getTransactionInfo() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("service", "transaction-service");
        response.put("port", port);
        response.put("message", "Request processed by Transaction Service");
        return response;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody TransferRequest request) {
        TransferResponse response = transactionService.transfer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        if (transaction == null) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "FAILED");
            error.put("message", "Transaction not found: " + id);
            return ResponseEntity.status(404).body(error);
        }
        return ResponseEntity.ok(transaction);
    }
}