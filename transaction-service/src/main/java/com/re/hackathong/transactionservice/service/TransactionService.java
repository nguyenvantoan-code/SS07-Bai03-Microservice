package com.re.hackathong.transactionservice.service;

import com.re.hackathong.transactionservice.dto.AccountDto;
import com.re.hackathong.transactionservice.dto.AmountRequest;
import com.re.hackathong.transactionservice.dto.TransferRequest;
import com.re.hackathong.transactionservice.dto.TransferResponse;
import com.re.hackathong.transactionservice.model.Transaction;
import com.re.hackathong.transactionservice.model.TransactionStatus;
import com.re.hackathong.transactionservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final String ACCOUNT_SERVICE_URL = "http://account-service/api/accounts/";

    private final RestTemplate restTemplate;
    private final TransactionRepository transactionRepository;

    public TransactionService(RestTemplate restTemplate, TransactionRepository transactionRepository) {
        this.restTemplate = restTemplate;
        this.transactionRepository = transactionRepository;
    }

    public TransferResponse transfer(TransferRequest request) {
        // Validation cơ bản
        if (request.getFromAccountNumber() == null || request.getFromAccountNumber().trim().isEmpty()) {
            return recordFailedTransaction(request, "fromAccountNumber is required");
        }
        if (request.getToAccountNumber() == null || request.getToAccountNumber().trim().isEmpty()) {
            return recordFailedTransaction(request, "toAccountNumber is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return recordFailedTransaction(request, "Amount must be greater than zero");
        }
        if (request.getFromAccountNumber().trim().equals(request.getToAccountNumber().trim())) {
            return recordFailedTransaction(request, "Source and destination accounts must be different");
        }

        String fromAcc = request.getFromAccountNumber().trim();
        String toAcc = request.getToAccountNumber().trim();
        Double amount = request.getAmount();

        // Bước 2: Kiểm tra tài khoản nguồn
        AccountDto sourceAccount;
        try {
            sourceAccount = restTemplate.getForObject(ACCOUNT_SERVICE_URL + fromAcc, AccountDto.class);
            if (sourceAccount == null) {
                return recordFailedTransaction(request, "Source account not found");
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Source account not found: {}", fromAcc);
            return recordFailedTransaction(request, "Source account not found");
        } catch (ResourceAccessException e) {
            log.error("Account service is unavailable when fetching source account", e);
            return recordFailedTransaction(request, "Account Service is unavailable");
        } catch (Exception e) {
            log.error("Error fetching source account: {}", e.getMessage());
            return recordFailedTransaction(request, "Error communicating with Account Service");
        }

        // Bước 3: Kiểm tra tài khoản đích
        try {
            AccountDto destAccount = restTemplate.getForObject(ACCOUNT_SERVICE_URL + toAcc, AccountDto.class);
            if (destAccount == null) {
                return recordFailedTransaction(request, "Destination account not found");
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Destination account not found: {}", toAcc);
            return recordFailedTransaction(request, "Destination account not found");
        } catch (ResourceAccessException e) {
            log.error("Account service is unavailable when fetching destination account", e);
            return recordFailedTransaction(request, "Account Service is unavailable");
        } catch (Exception e) {
            log.error("Error fetching destination account: {}", e.getMessage());
            return recordFailedTransaction(request, "Error communicating with Account Service");
        }

        // Bước 4: Kiểm tra số dư tài khoản nguồn
        if (sourceAccount.getBalance() == null || sourceAccount.getBalance() < amount) {
            log.warn("Insufficient balance for account {}. Current: {}, Required: {}",
                    fromAcc, sourceAccount.getBalance(), amount);
            return recordFailedTransaction(request, "Insufficient balance");
        }

        // Bước 5: Thực hiện debit tài khoản nguồn và credit tài khoản đích
        try {
            // Debit tài khoản nguồn
            AmountRequest amountRequest = new AmountRequest(amount);
            HttpEntity<AmountRequest> debitEntity = new HttpEntity<>(amountRequest);
            ResponseEntity<AccountDto> debitResponse = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL + fromAcc + "/debit",
                    HttpMethod.PUT,
                    debitEntity,
                    AccountDto.class
            );

            if (debitResponse.getStatusCode() != HttpStatus.OK) {
                return recordFailedTransaction(request, "Debit operation failed on source account");
            }

            // Credit tài khoản đích
            HttpEntity<AmountRequest> creditEntity = new HttpEntity<>(amountRequest);
            ResponseEntity<AccountDto> creditResponse = restTemplate.exchange(
                    ACCOUNT_SERVICE_URL + toAcc + "/credit",
                    HttpMethod.PUT,
                    creditEntity,
                    AccountDto.class
            );

            if (creditResponse.getStatusCode() != HttpStatus.OK) {
                // Hoàn tiền cho tài khoản nguồn nếu credit thất bại
                try {
                    restTemplate.exchange(ACCOUNT_SERVICE_URL + fromAcc + "/credit", HttpMethod.PUT, debitEntity, AccountDto.class);
                } catch (Exception ex) {
                    log.error("Critical: Failed to compensate debit on {}", fromAcc, ex);
                }
                return recordFailedTransaction(request, "Credit operation failed on destination account");
            }

            // Bước 6: Lưu transaction SUCCESS
            Transaction tx = new Transaction(
                    null,
                    fromAcc,
                    toAcc,
                    amount,
                    request.getDescription(),
                    TransactionStatus.SUCCESS,
                    null
            );
            Transaction saved = transactionRepository.save(tx);
            log.info("Transfer successful. Transaction ID: {}", saved.getId());

            return TransferResponse.success(
                    saved.getId(),
                    fromAcc,
                    toAcc,
                    amount,
                    "Transfer successful"
            );

        } catch (HttpClientErrorException e) {
            log.error("Client error during debit/credit: {}", e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("Insufficient balance")) {
                return recordFailedTransaction(request, "Insufficient balance");
            }
            return recordFailedTransaction(request, "Transaction rejected by Account Service");
        } catch (ResourceAccessException e) {
            log.error("Account service unavailable during debit/credit", e);
            return recordFailedTransaction(request, "Account Service is unavailable");
        } catch (Exception e) {
            log.error("Unexpected error during transfer", e);
            return recordFailedTransaction(request, "Transfer failed: " + e.getMessage());
        }
    }

    private TransferResponse recordFailedTransaction(TransferRequest request, String errorMessage) {
        Transaction tx = new Transaction(
                null,
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount(),
                request.getDescription(),
                TransactionStatus.FAILED,
                errorMessage
        );
        transactionRepository.save(tx);
        return TransferResponse.failed(errorMessage);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }
}
