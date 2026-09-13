package com.re.hackathong.accountservice.repository;

import com.re.hackathong.accountservice.model.Account;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void initData() {
        save(new Account(idGenerator.getAndIncrement(), "1001", 10000000.0));
        save(new Account(idGenerator.getAndIncrement(), "1002", 5000000.0));
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accounts.get(accountNumber));
    }

    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            account.setId(idGenerator.getAndIncrement());
        }
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return accountNumber != null && accounts.containsKey(accountNumber);
    }
}
