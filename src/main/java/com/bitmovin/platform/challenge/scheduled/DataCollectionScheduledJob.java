package com.bitmovin.platform.challenge.scheduled;

import com.bitmovin.platform.challenge.entity.UsageSnapshot;
import com.bitmovin.platform.challenge.service.CloudWatchService;
import com.bitmovin.platform.challenge.service.CostExplorerService;
import com.bitmovin.platform.challenge.service.UsageMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataCollectionScheduledJob {

    private final CloudWatchService cloudWatchService;
    private final CostExplorerService costExplorerService;
    private final UsageMetricsService metricsService;

    /**
     * Runs every 5 minutes to collect CloudWatch data
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @SchedulerLock(
            name = "dataCollectionJob",
            lockAtMostFor = "PT10M",
            lockAtLeastFor = "PT2M"
    )
    public void collectUsageData() {
        log.debug("Starting data collection job");

        try {
            // Collect CloudWatch data (last 3 hours)
            Instant to = Instant.now();
            Instant from = to.minus(Duration.ofHours(3));

            List<UsageSnapshot> cloudWatchSnapshots =
                    cloudWatchService.retrieveAndStoreRecentUsage(from, to);

            log.info("Collected {} CloudWatch snapshots", cloudWatchSnapshots.size());
            metricsService.recordDataCollection("cloudwatch", cloudWatchSnapshots.size());

        } catch (Exception e) {
            log.error("Failed to collect CloudWatch data", e);
        }
    }

    /**
     * Runs daily to collect Cost Explorer data for yesterday
     * (Cost Explorer data is delayed by ~24 hours)
     */
    @Scheduled(cron = "0 30 1 * * *") // 1:30 AM UTC daily
    @SchedulerLock(
            name = "costExplorerCollectionJob",
            lockAtMostFor = "PT30M",
            lockAtLeastFor = "PT5M"
    )
    public void collectBillingData() {
        log.info("Starting Cost Explorer data collection");

        try {
            // Collect yesterday's data (24h delay)
            Instant to = Instant.now().minus(Duration.ofDays(1))
                    .truncatedTo(ChronoUnit.DAYS);
            Instant from = to.minus(Duration.ofDays(1));

            List<UsageSnapshot> costExplorerSnapshots =
                    costExplorerService.retrieveAndStoreBillingUsage(from, to);

            log.info("Collected {} Cost Explorer snapshots", costExplorerSnapshots.size());
            metricsService.recordDataCollection("costexplorer", costExplorerSnapshots.size());

        } catch (Exception e) {
            log.error("Failed to collect Cost Explorer data", e);
        }
    }
}
