package com.bitmovin.platform.challenge.repository;

import com.bitmovin.platform.challenge.entity.Distribution;
import com.bitmovin.platform.challenge.entity.enums.DistributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, String> {
    List<Distribution> findByStatus(DistributionStatus status);
    List<Distribution> findByCustomerId(String customerId);
}