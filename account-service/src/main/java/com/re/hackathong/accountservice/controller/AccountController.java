package com.re.hackathong.accountservice.controller;

import com.re.hackathong.accountservice.dto.AccountResponse;
import com.re.hackathong.accountservice.dto.AmountRequest;
import com.re.hackathong.accountservice.model.Account;
import com.re.hackathong.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Value("${server.port:8082}")
    private String port;

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/info")
    public Map<String, String> getAccountInfo() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("service", "account-service");
        response.put("port", port);
        response.put("message", "Request processed by Account Service");
        return response;
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(new AccountResponse(account.getAccountNumber(), account.getBalance(), account.getCustomerId()));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountResponse> getBalance(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(new AccountResponse(accountNumber, account.getBalance(), account.getCustomerId()));
    }

    @PutMapping("/{accountNumber}/debit")
    public ResponseEntity<AccountResponse> debit(
            @PathVariable String accountNumber,
            @RequestBody AmountRequest request) {
        Double amount = request != null ? request.getAmount() : null;
        Account updatedAccount = accountService.debit(accountNumber, amount);
        return ResponseEntity.ok(new AccountResponse(
                updatedAccount.getAccountNumber(),
                updatedAccount.getBalance(),
                "Debit successful",
                updatedAccount.getCustomerId()
        ));
    }

    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<AccountResponse> credit(
            @PathVariable String accountNumber,
            @RequestBody AmountRequest request) {
        Double amount = request != null ? request.getAmount() : null;
        Account updatedAccount = accountService.credit(accountNumber, amount);
        return ResponseEntity.ok(new AccountResponse(
                updatedAccount.getAccountNumber(),
                updatedAccount.getBalance(),
                "Credit successful",
                updatedAccount.getCustomerId()
        ));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account created = accountService.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}