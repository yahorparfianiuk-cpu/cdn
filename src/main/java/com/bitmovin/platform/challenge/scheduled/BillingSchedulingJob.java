package com.bitmovin.platform.challenge.scheduled;

import com.bitmovin.platform.challenge.entity.Customer;
import com.bitmovin.platform.challenge.repository.CustomerRepository;
import com.bitmovin.platform.challenge.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class BillingSchedulingJob {


    private final CustomerService customerService;

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
    public void checkBilling() {
        log.info("Starting billing job execution");
        Instant jobStart = Instant.now();


        customerService.findAll()
                .forEach(this::checkCustomerBilling);

    }

    protected void checkCustomerBilling(Customer customer) {
        String id = customer.getCustomerId();

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
