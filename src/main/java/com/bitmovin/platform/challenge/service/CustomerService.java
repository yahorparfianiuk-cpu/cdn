package com.bitmovin.platform.challenge.service;

import com.bitmovin.platform.challenge.entity.Customer;
import com.bitmovin.platform.challenge.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }
}
