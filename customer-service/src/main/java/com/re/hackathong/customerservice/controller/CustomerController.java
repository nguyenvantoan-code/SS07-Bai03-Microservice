package com.re.hackathong.customerservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @GetMapping
    public String getCustomers() {
        return "Customer Service - GET customers";
    }

    @GetMapping("/{id}")
    public String getCustomerById(@PathVariable Long id) {
        return "Customer Service - Customer ID: " + id;
    }

    @PostMapping
    public String createCustomer() {
        return "Customer Service - Customer created successfully";
    }
}