package com.bitmovin.platform.challenge.application;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import com.bitmovin.platform.challenge.domain.UsageRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class UsageCapService {

    private final UsageRepository usageRepository;

    public UsageCapService(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkLimits() {
        var now = Instant.now();
        for (String customerId : usageRepository.getAllCustomerIds()) {
            double usage15m = usageRepository.findByCustomerAndPeriod(customerId, now.minus(Duration.ofMinutes(15)), now)
                    .stream().mapToDouble(DailyEntity::usageGb).sum();
            double usage3h = usageRepository.findByCustomerAndPeriod(customerId, now.minus(Duration.ofHours(3)), now)
                    .stream().mapToDouble(DailyEntity::usageGb).sum();

            if (usage15m > 100 || usage3h > 500) {
                // disable CDN and publish event
                System.out.println("⚠️ CDN capped for " + customerId);
            }
        }
    }
}

