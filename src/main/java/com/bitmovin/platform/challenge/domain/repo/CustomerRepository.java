package com.bitmovin.platform.challenge.domain.repo;

import com.bitmovin.platform.challenge.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
