package com.bitmovin.platform.challenge.domain.repo;

import com.bitmovin.platform.challenge.domain.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionRepository extends JpaRepository<Distribution, String> {
}