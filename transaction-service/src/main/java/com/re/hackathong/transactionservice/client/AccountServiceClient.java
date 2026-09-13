package com.re.hackathong.transactionservice.client;

import com.re.hackathong.transactionservice.dto.AccountResponse;
import com.re.hackathong.transactionservice.dto.AmountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{accountNumber}")
    AccountResponse getAccount(@PathVariable("accountNumber") String accountNumber);

    @GetMapping("/api/accounts/{accountNumber}/balance")
    AccountResponse getBalance(@PathVariable("accountNumber") String accountNumber);

    @PutMapping("/api/accounts/{accountNumber}/debit")
    AccountResponse debit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody AmountRequest request
    );

    @PutMapping("/api/accounts/{accountNumber}/credit")
    AccountResponse credit(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody AmountRequest request
    );
}
