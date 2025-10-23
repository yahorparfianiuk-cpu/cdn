package com.bitmovin.platform.challenge.application;

import com.bitmovin.platform.challenge.domain.Distribution;
import com.bitmovin.platform.challenge.domain.repo.DistributionRepository;
import com.bitmovin.platform.challenge.infrastructure.aws.CloudWatchClient;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ShortTermUsageService {

    private final CloudWatchClient cloudWatchClient;
    private final DistributionRepository distributionRepo;

    private static final double LIMIT_15MIN = 100.0;
    private static final double LIMIT_3H = 500.0;

    public ShortTermUsageService(CloudWatchClient cloudWatchClient, DistributionRepository distributionRepo) {
        this.cloudWatchClient = cloudWatchClient;
        this.distributionRepo = distributionRepo;
    }

    public void checkCapping() {
        Instant now = Instant.now();
        Instant from15Min = now.minusSeconds(15 * 60);
        Instant from3h = now.minusSeconds(3 * 3600);

        var metrics = cloudWatchClient.getMetrics(from3h, now);
        for (var result : metrics.metricDataResults()) {
            String distributionId = result.label().replace("Distribution ", "");
            double usage15Min = result.values().stream().mapToDouble(Double::doubleValue).sum(); // simplification
            double usage3h = result.values().stream().mapToDouble(Double::doubleValue).sum();

            if (usage15Min > LIMIT_15MIN || usage3h > LIMIT_3H) {
                Distribution distribution = distributionRepo.findById(distributionId).orElseThrow();
                distribution.setActive(false);
                distributionRepo.save(distribution);
                System.out.println("CAPPED: " + distributionId
                        + " Usage15min=" + usage15Min + "GB, Usage3h=" + usage3h + "GB");
            }
        }
    }
}
