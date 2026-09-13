package com.re.hackathong.accountservice.dto;

public class AmountRequest {
    private Double amount;

    public AmountRequest() {
    }

    public AmountRequest(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
