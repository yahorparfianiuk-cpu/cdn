package com.bitmovin.platform.challenge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Customer {
    @Id
    private String id; // X-Customer-Id
    private String name;
}