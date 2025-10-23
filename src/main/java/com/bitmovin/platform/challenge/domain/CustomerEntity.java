package com.bitmovin.platform.challenge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CustomerEntity {

    @Id
    private String customerId;
    private String name;

    private String bucketId;
    private String distributionId;

    public CustomerEntity() {}
    public CustomerEntity(String customerId, String name, String bucketId, String distributionId) {
        this.customerId = customerId;
        this.name = name;
        this.bucketId = bucketId;
        this.distributionId = distributionId;
    }

    // getters/setters
}
