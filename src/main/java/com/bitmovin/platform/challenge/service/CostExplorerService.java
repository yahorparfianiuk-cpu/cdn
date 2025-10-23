package com.bitmovin.platform.challenge.service;

import com.bitmovin.platform.challenge.aws.CostExplorerClient;
import com.bitmovin.platform.challenge.entity.Distribution;
import com.bitmovin.platform.challenge.entity.UsageSnapshot;
import com.bitmovin.platform.challenge.entity.enums.UsageDataSource;
import com.bitmovin.platform.challenge.repository.DistributionRepository;
import com.bitmovin.platform.challenge.repository.UsageSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CostExplorerService {
    
    private final CostExplorerClient costExplorerClient;
    private final UsageSnapshotRepository usageSnapshotRepository;
    private final DistributionRepository distributionRepository;
    
    /**
     * Retrieve billing-accurate usage from Cost Explorer
     * Note: Data is delayed by ~24 hours
     */
    public List<UsageSnapshot> retrieveAndStoreBillingUsage(Instant from, Instant to) {
        log.info("Retrieving Cost Explorer data from {} to {}", from, to);
        
        List<UsageSnapshot> snapshots = new ArrayList<>();
        List<Distribution> distributions = distributionRepository.findAll();
        
        for (Distribution distribution : distributions) {
            GetCostAndUsageResponse response = costExplorerClient.retrieveUsage(
                from, to, distribution.getDistributionId()
            );
            
            Double dataTransferBytes = extractDataTransferBytes(response);
            Double usageGb = dataTransferBytes / (1024.0 * 1024.0 * 1024.0);
            
            UsageSnapshot snapshot = new UsageSnapshot();
            snapshot.setDistributionId(distribution.getDistributionId());
            snapshot.setCustomerId(distribution.getCustomerId());
            snapshot.setSnapshotTime(Instant.now());
            snapshot.setDataTransferGb(usageGb);
            snapshot.setSource(UsageDataSource.COST_EXPLORER);
            snapshot.setPeriodStart(from);
            snapshot.setPeriodEnd(to);
            
            snapshots.add(usageSnapshotRepository.save(snapshot));
        }
        
        log.info("Stored {} Cost Explorer snapshots", snapshots.size());
        return snapshots;
    }
    
    /**
     * Get aggregated usage per customer for billing
     * Uses denormalized customer_id in usage_snapshot for fast queries
     * @return usage by customer id
     */
    public Map<String, Double> getCustomerUsageForBilling(Instant from, Instant to) {
        Map<String, Double> customerUsage = new HashMap<>();
        
        List<Object[]> results = usageSnapshotRepository.sumUsageByCustomerAndSource(
            from, to, UsageDataSource.COST_EXPLORER
        );
        
        for (Object[] result : results) {
            String customerId = (String) result[0];
            Double usage = (Double) result[1];
            customerUsage.put(customerId, usage != null ? usage : 0.0);
        }
        
        return customerUsage;
    }
    
    private Double extractDataTransferBytes(GetCostAndUsageResponse response) {
        return response.resultsByTime().stream()
            .flatMap(result -> result.groups().stream())
            .filter(group -> group.keys().contains("-DataTransfer-Out-Bytes"))
            .findFirst()
            .map(group -> group.metrics().get("UsageQuantity"))
            .map(metricValue -> Double.parseDouble(metricValue.amount()))
            .orElse(0.0);
    }
}
