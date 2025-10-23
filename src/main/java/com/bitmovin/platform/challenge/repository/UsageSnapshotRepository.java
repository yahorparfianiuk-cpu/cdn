package com.bitmovin.platform.challenge.repository;

import com.bitmovin.platform.challenge.entity.UsageSnapshot;
import com.bitmovin.platform.challenge.entity.enums.UsageDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UsageSnapshotRepository extends JpaRepository<UsageSnapshot, Long> {
    
    @Query("SELECT u FROM UsageSnapshot u WHERE u.customerId = :customerId " +
           "AND u.snapshotTime >= :from AND u.snapshotTime <= :to " +
           "ORDER BY u.snapshotTime")
    List<UsageSnapshot> findByCustomerAndTimeRange(
        @Param("customerId") String customerId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );
    
    @Query("SELECT SUM(u.dataTransferGb) FROM UsageSnapshot u " +
           "WHERE u.distributionId = :distributionId " +
           "AND u.snapshotTime >= :from AND u.snapshotTime <= :to " +
           "AND u.source = :source")
    Double sumUsageByDistributionAndTimeRange(
        @Param("distributionId") String distributionId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("source") UsageDataSource source
    );
    
    @Query("SELECT u.customerId, SUM(u.dataTransferGb) FROM UsageSnapshot u " +
           "WHERE u.snapshotTime >= :from AND u.snapshotTime <= :to " +
           "AND u.source = :source " +
           "GROUP BY u.customerId")
    List<Object[]> sumUsageByCustomerAndSource(
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("source") UsageDataSource source
    );
}