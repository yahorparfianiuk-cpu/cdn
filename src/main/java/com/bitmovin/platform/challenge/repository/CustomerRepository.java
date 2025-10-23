package com.bitmovin.platform.challenge.repository;

import com.bitmovin.platform.challenge.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
}