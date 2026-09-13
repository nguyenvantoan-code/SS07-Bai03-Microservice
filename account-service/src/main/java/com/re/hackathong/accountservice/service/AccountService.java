package com.re.hackathong.accountservice.service;

import com.re.hackathong.accountservice.exception.AccountNotFoundException;
import com.re.hackathong.accountservice.exception.InsufficientBalanceException;
import com.re.hackathong.accountservice.exception.InvalidAmountException;
import com.re.hackathong.accountservice.model.Account;
import com.re.hackathong.accountservice.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    public Double getBalance(String accountNumber) {
        return getAccount(accountNumber).getBalance();
    }

    public synchronized Account debit(String accountNumber, Double amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("Debit amount must be greater than zero");
        }

        Account account = getAccount(accountNumber);
        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance() - amount);
        return accountRepository.save(account);
    }

    public synchronized Account credit(String accountNumber, Double amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidAmountException("Credit amount must be greater than zero");
        }

        Account account = getAccount(accountNumber);
        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(Account account) {
        if (accountRepository.existsByAccountNumber(account.getAccountNumber())) {
            throw new InvalidAmountException("Account number already exists: " + account.getAccountNumber());
        }
        if (account.getBalance() == null || account.getBalance() < 0) {
            account.setBalance(0.0);
        }
        return accountRepository.save(account);
    }
}
