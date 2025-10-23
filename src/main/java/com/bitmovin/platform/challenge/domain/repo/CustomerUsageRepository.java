package com.bitmovin.platform.challenge.domain.repo;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CustomerUsageRepository extends JpaRepository<DailyEntity, Long> {

    // Get all usage for a customer between two dates (inclusive)
    List<DailyEntity> findByCustomerIdAndDateBetween(String customerId, LocalDate from, LocalDate to);

    // Get all usage for a distribution after a specific date
    List<DailyEntity> findByDistributionIdAndDateAfter(String distributionId, LocalDate from);
}
