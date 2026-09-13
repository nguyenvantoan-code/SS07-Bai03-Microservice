package com.re.hackathong.transactionservice.client;

import com.re.hackathong.transactionservice.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomer(@PathVariable("id") Long id);
}
