package com.re.hackathong.transactionservice.service;

import com.re.hackathong.transactionservice.client.AccountServiceClient;
import com.re.hackathong.transactionservice.client.CustomerServiceClient;
import com.re.hackathong.transactionservice.dto.*;
import com.re.hackathong.transactionservice.model.Transaction;
import com.re.hackathong.transactionservice.model.TransactionStatus;
import com.re.hackathong.transactionservice.repository.TransactionRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final AccountServiceClient accountServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountServiceClient accountServiceClient,
            CustomerServiceClient customerServiceClient,
            TransactionRepository transactionRepository
    ) {
        this.accountServiceClient = accountServiceClient;
        this.customerServiceClient = customerServiceClient;
        this.transactionRepository = transactionRepository;
    }

    public TransferResponse transfer(TransferRequest request) {
        // 1. Validation đầu vào cơ bản
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

        // 2. Kiểm tra tài khoản nguồn qua AccountServiceClient
        AccountResponse sourceAccount;
        try {
            sourceAccount = accountServiceClient.getAccount(fromAcc);
            if (sourceAccount == null) {
                return recordFailedTransaction(request, "Source account not found");
            }
        } catch (FeignException.NotFound e) {
            log.warn("Source account not found via Feign: {}", fromAcc);
            return recordFailedTransaction(request, "Source account not found");
        } catch (FeignException e) {
            log.error("Account service error when fetching source account", e);
            return recordFailedTransaction(request, "Account Service is unavailable");
        } catch (Exception e) {
            log.error("Unexpected error fetching source account", e);
            return recordFailedTransaction(request, "Error communicating with Account Service");
        }

        // 3. Kiểm tra tài khoản đích qua AccountServiceClient
        try {
            AccountResponse destAccount = accountServiceClient.getAccount(toAcc);
            if (destAccount == null) {
                return recordFailedTransaction(request, "Destination account not found");
            }
        } catch (FeignException.NotFound e) {
            log.warn("Destination account not found via Feign: {}", toAcc);
            return recordFailedTransaction(request, "Destination account not found");
        } catch (FeignException e) {
            log.error("Account service error when fetching destination account", e);
            return recordFailedTransaction(request, "Account Service is unavailable");
        } catch (Exception e) {
            log.error("Unexpected error fetching destination account", e);
            return recordFailedTransaction(request, "Error communicating with Account Service");
        }

        // 4. Kiểm tra số dư tài khoản nguồn
        if (sourceAccount.getBalance() == null || sourceAccount.getBalance() < amount) {
            log.warn("Insufficient balance for account {}. Current: {}, Required: {}",
                    fromAcc, sourceAccount.getBalance(), amount);
            return recordFailedTransaction(request, "Insufficient balance");
        }

        // 5. Thực hiện debit tài khoản nguồn và credit tài khoản đích qua FeignClient
        try {
            AmountRequest amountRequest = new AmountRequest(amount);

            // Debit tài khoản nguồn
            AccountResponse debitResponse = accountServiceClient.debit(fromAcc, amountRequest);
            if (debitResponse == null) {
                return recordFailedTransaction(request, "Debit operation failed on source account");
            }

            // Credit tài khoản đích
            try {
                AccountResponse creditResponse = accountServiceClient.credit(toAcc, amountRequest);
                if (creditResponse == null) {
                    // Bù tiền (compensate) nếu credit trả về null
                    compensateDebit(fromAcc, amountRequest);
                    return recordFailedTransaction(request, "Credit operation failed on destination account");
                }
            } catch (Exception ex) {
                log.error("Error during credit to destination account {}. Compensating debit...", toAcc, ex);
                compensateDebit(fromAcc, amountRequest);
                return recordFailedTransaction(request, "Credit operation failed on destination account");
            }

            // 6. Lưu transaction SUCCESS
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
            log.info("Transfer successful via FeignClient. Transaction ID: {}", saved.getId());

            return TransferResponse.success(
                    saved.getId(),
                    fromAcc,
                    toAcc,
                    amount,
                    "Transfer successful"
            );

        } catch (FeignException.BadRequest e) {
            log.error("Bad request during debit/credit: {}", e.contentUTF8());
            if (e.contentUTF8() != null && e.contentUTF8().contains("Insufficient balance")) {
                return recordFailedTransaction(request, "Insufficient balance");
            }
            return recordFailedTransaction(request, "Transaction rejected by Account Service");
        } catch (FeignException e) {
            log.error("Account service error during debit/credit", e);
            return recordFailedTransaction(request, "Account Service is unavailable");
        } catch (Exception e) {
            log.error("Unexpected error during transfer", e);
            return recordFailedTransaction(request, "Transfer failed: " + e.getMessage());
        }
    }

    private void compensateDebit(String fromAcc, AmountRequest amountRequest) {
        try {
            accountServiceClient.credit(fromAcc, amountRequest);
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to compensate debit on {}", fromAcc, ex);
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

    public TransactionDetailResponse getTransactionDetail(Long id) {
        Transaction tx = transactionRepository.findById(id).orElse(null);
        if (tx == null) {
            return null;
        }

        CustomerResponse customer = null;
        try {
            // Lấy thông tin tài khoản nguồn từ Account Service
            AccountResponse sourceAccount = accountServiceClient.getAccount(tx.getFromAccountNumber());
            Long customerId = (sourceAccount != null && sourceAccount.getCustomerId() != null)
                    ? sourceAccount.getCustomerId()
                    : 1L;

            // Gọi Customer Service qua FeignClient để lấy thông tin khách hàng
            customer = customerServiceClient.getCustomer(customerId);
        } catch (FeignException.NotFound e) {
            log.warn("Customer or account not found for transaction id: {}", id);
        } catch (FeignException e) {
            log.error("Error communicating with microservice for transaction detail: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving customer for transaction detail", e);
        }

        return new TransactionDetailResponse(
                tx.getId(),
                tx.getFromAccountNumber(),
                tx.getToAccountNumber(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getStatus() != null ? tx.getStatus().name() : null,
                customer
        );
    }
}
