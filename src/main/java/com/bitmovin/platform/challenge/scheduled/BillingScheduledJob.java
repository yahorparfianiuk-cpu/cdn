package com.bitmovin.platform.challenge.scheduled;

import com.bitmovin.platform.challenge.entity.BillingEvent;
import com.bitmovin.platform.challenge.entity.Distribution;
import com.bitmovin.platform.challenge.entity.enums.BillingEventStatus;
import com.bitmovin.platform.challenge.repository.BillingEventRepository;
import com.bitmovin.platform.challenge.repository.DistributionRepository;
import com.bitmovin.platform.challenge.service.BillingEventPublisher;
import com.bitmovin.platform.challenge.service.CostExplorerService;
import com.bitmovin.platform.challenge.service.UsageMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class BillingScheduledJob {

    private final BillingEventRepository billingEventRepository;
    private final DistributionRepository distributionRepository;
    private final CostExplorerService costExplorerService;
    private final BillingEventPublisher billingEventPublisher;
    private final UsageMetricsService metricsService;

    /**
     * Runs every 24 hours at midnight UTC
     * Only one instance will execute due to ShedLock
     */
    @Scheduled(cron = "0 0 0 * * *") // Midnight UTC daily
    @SchedulerLock(
            name = "billingJob",
            lockAtMostFor = "PT30M",  // Release lock after 30 min max
            lockAtLeastFor = "PT5M"   // Hold lock for at least 5 min
    )
    public void sendBillingEvents() {
        log.info("Starting billing job execution");
        Instant jobStart = Instant.now();
        int processedCount = 0;
        int errorCount = 0;

        // Get all distinct customer IDs from billing history
        List<String> customerIds = billingEventRepository.findAllDistinctCustomerIds();

        // If no history, get from distributions
        if (customerIds.isEmpty()) {
            customerIds = distributionRepository.findAll().stream()
                    .map(Distribution::getCustomerId)
                    .distinct()
                    .collect(Collectors.toList());
        }

        for (String customerId : customerIds) {
            try {
                processBillingForCustomer(customerId);
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to process billing for customer {}", customerId, e);
                errorCount++;
            }
        }

        Duration elapsed = Duration.between(jobStart, Instant.now());
        log.info("Billing job completed. Processed: {}, Errors: {}, Duration: {}s",
                processedCount, errorCount, elapsed.getSeconds());

        metricsService.recordBillingJobExecution(processedCount, errorCount, elapsed);
    }

    @Transactional
    protected void processBillingForCustomer(String customerId) {
        // Find last billing period end, default to 24 hours ago
        Instant periodEnd = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant periodStart = billingEventRepository
                .findLastBillingPeriodEnd(customerId)
                .orElse(periodEnd.minus(Duration.ofDays(1)));

        log.info("Processing billing for customer {} from {} to {}",
                customerId, periodStart, periodEnd);

        // Check if billing event already exists (idempotency)
        Optional<BillingEvent> existing = billingEventRepository
                .findByCustomerIdAndPeriodStartAndPeriodEnd(customerId, periodStart, periodEnd);

        if (existing.isPresent()) {
            log.info("Billing event already exists for customer {} (period {} to {}), skipping",
                    customerId, periodStart, periodEnd);
            return;
        }

        // Calculate usage from Cost Explorer (accurate billing data)
        Map<String, Double> usage = costExplorerService
                .getCustomerUsageForBilling(periodStart, periodEnd);
        Double totalUsageGb = usage.getOrDefault(customerId, 0.0);

        // Create billing event record
        BillingEvent event = new BillingEvent();
        event.setCustomerId(customerId);
        event.setPeriodStart(periodStart);
        event.setPeriodEnd(periodEnd);
        event.setTrafficUsageGb(totalUsageGb);
        event.setStatus(BillingEventStatus.PENDING);

        billingEventRepository.save(event);

        // Publish to billing system
        billingEventPublisher.publishBillingEvent(event);

        // Mark as sent
        event.setStatus(BillingEventStatus.SENT);
        event.setSentAt(Instant.now());
        billingEventRepository.save(event);

        log.info("Billing event sent for customer {}: {} GB", customerId, totalUsageGb);
        metricsService.recordBillingEvent(customerId, totalUsageGb);
    }
}
