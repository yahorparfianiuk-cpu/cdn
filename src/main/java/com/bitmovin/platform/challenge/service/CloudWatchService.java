package com.bitmovin.platform.challenge.service;

import com.bitmovin.platform.challenge.aws.CloudWatchClient;
import com.bitmovin.platform.challenge.entity.Distribution;
import com.bitmovin.platform.challenge.entity.UsageSnapshot;
import com.bitmovin.platform.challenge.entity.enums.UsageDataSource;
import com.bitmovin.platform.challenge.repository.DistributionRepository;
import com.bitmovin.platform.challenge.repository.UsageSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudWatchService {
    
    private final CloudWatchClient cloudWatchClient;
    private final UsageSnapshotRepository usageSnapshotRepository;
    private final DistributionRepository distributionRepository;
    
    /**
     * Retrieve recent usage from CloudWatch and store as snapshots
     * @param from Start time (typically NOW - 3 hours)
     * @param to End time (typically NOW)
     * @return List of created usage snapshots
     */
    public List<UsageSnapshot> retrieveAndStoreRecentUsage(Instant from, Instant to) {
        log.info("Retrieving CloudWatch metrics from {} to {}", from, to);
        
        GetMetricDataResponse response = cloudWatchClient.getMetrics(from, to);
        List<UsageSnapshot> snapshots = new ArrayList<>();
        
        for (MetricDataResult result : response.metricDataResults()) {
            String distributionId = extractDistributionId(result.label());
            
            // Look up distribution to get customer_id
            Distribution distribution = distributionRepository.findById(distributionId)
                .orElse(null);
            
            if (distribution == null) {
                log.warn("Distribution not found: {}", distributionId);
                continue;
            }
            
            // CloudWatch returns values in bytes, convert to GB
            Double usageGb = result.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum() / (1024.0 * 1024.0 * 1024.0);
            
            UsageSnapshot snapshot = new UsageSnapshot();
            snapshot.setDistributionId(distributionId);
            snapshot.setCustomerId(distribution.getCustomerId());
            snapshot.setSnapshotTime(Instant.now());
            snapshot.setDataTransferGb(usageGb);
            snapshot.setSource(UsageDataSource.CLOUDWATCH);
            snapshot.setPeriodStart(from);
            snapshot.setPeriodEnd(to);
            
            snapshots.add(usageSnapshotRepository.save(snapshot));
        }
        
        log.info("Stored {} CloudWatch snapshots", snapshots.size());
        return snapshots;
    }
    
    /**
     * Calculate usage for a distribution within a sliding window
     * Used for usage capping checks (15min, 3hr windows)
     */
    public Double getUsageForWindow(String distributionId, Duration window) {
        Instant to = Instant.now();
        Instant from = to.minus(window);
        
        Double usage = usageSnapshotRepository.sumUsageByDistributionAndTimeRange(
            distributionId, from, to, UsageDataSource.CLOUDWATCH
        );
        
        return usage != null ? usage : 0.0;
    }
    
    /**
     * Check if distribution usage exceeds threshold within window
     */
    public boolean exceedsThreshold(String distributionId, Double thresholdGb, Duration window) {
        Double usage = getUsageForWindow(distributionId, window);
        boolean exceeds = usage >= thresholdGb;
        
        if (exceeds) {
            log.warn("Distribution {} exceeded threshold: {} GB (limit: {} GB) in {}",
                distributionId, usage, thresholdGb, window);
        }
        
        return exceeds;
    }
    
    private String extractDistributionId(String label) {
        // Label format: "Distribution dist-1"
        return label.substring(label.lastIndexOf(" ") + 1);
    }
}
