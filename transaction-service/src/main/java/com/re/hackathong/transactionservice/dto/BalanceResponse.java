package com.re.hackathong.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BalanceResponse {
    private String accountNumber;
    private Double balance;
    private Long customerId;

    public BalanceResponse() {
    }

    public BalanceResponse(String accountNumber, Double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public BalanceResponse(String accountNumber, Double balance, Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
}
