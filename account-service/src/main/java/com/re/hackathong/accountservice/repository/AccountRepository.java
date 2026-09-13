package com.re.hackathong.accountservice.repository;

import com.re.hackathong.accountservice.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByAccountNumber(String accountNumber);
    Account save(Account account);
    List<Account> findAll();
    boolean existsByAccountNumber(String accountNumber);
}
