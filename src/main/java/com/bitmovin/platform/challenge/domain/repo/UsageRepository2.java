package com.bitmovin.platform.challenge.domain.repo;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UsageRepository2 extends JpaRepository<DailyEntity, Long> {

    List<DailyEntity> findByCustomerIdAndTimestampBetween(String customerId, Instant from, Instant to);

    default List<DailyEntity> findByCustomerAndPeriod(String customerId, Instant from, Instant to) {
        return findByCustomerIdAndTimestampBetween(customerId, from, to);
    }
}
