package com.re.hackathong.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    private String accountNumber;
    private Double balance;
    private String message;
    private Long customerId;

    public AccountResponse() {
    }

    public AccountResponse(String accountNumber, Double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public AccountResponse(String accountNumber, Double balance, String message) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.message = message;
    }

    public AccountResponse(String accountNumber, Double balance, Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.customerId = customerId;
    }

    public AccountResponse(String accountNumber, Double balance, String message, Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.message = message;
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
