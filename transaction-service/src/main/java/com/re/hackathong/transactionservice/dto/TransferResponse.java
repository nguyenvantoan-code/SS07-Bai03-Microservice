package com.re.hackathong.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferResponse {
    private String status;
    private String message;
    private Long transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Double amount;

    public TransferResponse() {
    }

    public TransferResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public TransferResponse(String status, String message, Long transactionId,
                            String fromAccountNumber, String toAccountNumber, Double amount) {
        this.status = status;
        this.message = message;
        this.transactionId = transactionId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
    }

    public static TransferResponse success(Long transactionId, String fromAccountNumber,
                                           String toAccountNumber, Double amount, String message) {
        return new TransferResponse("SUCCESS", message, transactionId, fromAccountNumber, toAccountNumber, amount);
    }

    public static TransferResponse failed(String message) {
        return new TransferResponse("FAILED", message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
