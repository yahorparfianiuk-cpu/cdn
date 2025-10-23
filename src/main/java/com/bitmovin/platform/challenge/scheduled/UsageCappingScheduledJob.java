package com.bitmovin.platform.challenge.scheduled;

import com.bitmovin.platform.challenge.entity.Distribution;
import com.bitmovin.platform.challenge.entity.enums.DistributionStatus;
import com.bitmovin.platform.challenge.repository.DistributionRepository;
import com.bitmovin.platform.challenge.service.CdnDisableService;
import com.bitmovin.platform.challenge.service.CloudWatchService;
import com.bitmovin.platform.challenge.service.UsageMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UsageCappingScheduledJob {

    private final DistributionRepository distributionRepository;
    private final CloudWatchService cloudWatchService;
    private final CdnDisableService cdnDisableService;
    private final UsageMetricsService metricsService;

    // Thresholds
    private static final double LIMIT_15MIN_GB = 100.0;
    private static final double LIMIT_3HR_GB = 500.0;
    private static final Duration WINDOW_15MIN = Duration.ofMinutes(15);
    private static final Duration WINDOW_3HR = Duration.ofHours(3);

    /**
     * Runs every minute to check usage limits
     * Only one instance will execute due to ShedLock
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    @SchedulerLock(
            name = "usageCappingJob",
            lockAtMostFor = "PT5M",
            lockAtLeastFor = "PT30S"
    )
    public void checkUsageLimits() {
        log.debug("Starting usage capping check");

        List<Distribution> activeDistributions = distributionRepository
                .findByStatus(DistributionStatus.ACTIVE);

        int checkedCount = 0;
        int cappedCount = 0;

        for (Distribution distribution : activeDistributions) {
            try {
                boolean capped = checkAndCapDistribution(distribution);
                if (capped) {
                    cappedCount++;
                }
                checkedCount++;
            } catch (Exception e) {
                log.error("Error checking distribution {}",
                        distribution.getDistributionId(), e);
            }
        }

        log.debug("Usage capping check completed. Checked: {}, Capped: {}",
                checkedCount, cappedCount);
    }

    @Transactional
    protected boolean checkAndCapDistribution(Distribution distribution) {
        String distributionId = distribution.getDistributionId();

        // Check 15-minute limit
        if (cloudWatchService.exceedsThreshold(distributionId, LIMIT_15MIN_GB, WINDOW_15MIN)) {
            double usage = cloudWatchService.getUsageForWindow(distributionId, WINDOW_15MIN);
            String reason = String.format("Exceeded 100 GB limit in 15 minutes (%.2f GB)", usage);
            disableDistribution(distribution, reason, usage);
            metricsService.recordCappingEvent("15min", distributionId, usage);
            return true;
        }

        // Check 3-hour limit
        if (cloudWatchService.exceedsThreshold(distributionId, LIMIT_3HR_GB, WINDOW_3HR)) {
            double usage = cloudWatchService.getUsageForWindow(distributionId, WINDOW_3HR);
            String reason = String.format("Exceeded 500 GB limit in 3 hours (%.2f GB)", usage);
            disableDistribution(distribution, reason, usage);
            metricsService.recordCappingEvent("3hr", distributionId, usage);
            return true;
        }

        return false;
    }

    private void disableDistribution(Distribution distribution, String reason, double usageGb) {
        log.warn("Disabling distribution {}: {}", distribution.getDistributionId(), reason);

        distribution.setStatus(DistributionStatus.DISABLED);
        distribution.setDisabledAt(Instant.now());
        distribution.setDisableReason(reason);
        distributionRepository.save(distribution);

        cdnDisableService.disableDistribution(
                distribution.getDistributionId(), reason, usageGb
        );
    }
}
