package com.bitmovin.platform.challenge.repository;

import com.bitmovin.platform.challenge.entity.BillingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingEventRepository extends JpaRepository<BillingEvent, Long> {
    
    Optional<BillingEvent> findByCustomerIdAndPeriodStartAndPeriodEnd(
        String customerId, Instant periodStart, Instant periodEnd
    );
    
    @Query("SELECT MAX(b.periodEnd) FROM BillingEvent b " +
           "WHERE b.customerId = :customerId AND b.status = 'SENT'")
    Optional<Instant> findLastBillingPeriodEnd(@Param("customerId") String customerId);
    
    @Query("SELECT DISTINCT b.customerId FROM BillingEvent b")
    List<String> findAllDistinctCustomerIds();
}
