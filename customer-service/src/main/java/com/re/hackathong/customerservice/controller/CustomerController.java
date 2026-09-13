package com.re.hackathong.customerservice.controller;

import com.re.hackathong.customerservice.dto.CustomerResponse;
import com.re.hackathong.customerservice.model.Customer;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final Map<Long, Customer> customerDatabase = new ConcurrentHashMap<>();

    @PostConstruct
    public void initData() {
        customerDatabase.put(1L, new Customer(1L, "Nguyen Van A", "nguyenvana@finbank.com", "0901234567"));
        customerDatabase.put(2L, new Customer(2L, "Tran Thi B", "tranthib@finbank.com", "0907654321"));
    }

    @GetMapping
    public List<CustomerResponse> getCustomers() {
        List<CustomerResponse> responses = new ArrayList<>();
        for (Customer c : customerDatabase.values()) {
            responses.add(new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getPhone()));
        }
        return responses;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        Customer customer = customerDatabase.get(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone()));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody Customer customer) {
        if (customer.getId() == null) {
            customer.setId((long) (customerDatabase.size() + 1));
        }
        customerDatabase.put(customer.getId(), customer);
        return ResponseEntity.ok(new CustomerResponse(customer.getId(), customer.getName(), customer.getEmail(), customer.getPhone()));
    }
}