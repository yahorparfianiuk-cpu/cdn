package com.bitmovin.platform.challenge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class DailyEntity {

    @Id @GeneratedValue
    private Long id;

    private String customerId;
    private String distributionId;
    private LocalDate date;  // represents the full 24h period
    private BigDecimal usageGb;

    public DailyEntity() {
    }
    public DailyEntity(String customerId, String distributionId, LocalDate date, BigDecimal usageGb) {
        this.customerId = customerId;
        this.distributionId = distributionId;
        this.date = date;
        this.usageGb = usageGb;
    }
}
