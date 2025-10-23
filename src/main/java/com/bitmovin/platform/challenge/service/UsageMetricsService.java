package com.bitmovin.platform.challenge.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UsageMetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public void recordBillingEvent(String customerId, Double usageGb) {
        meterRegistry.counter("cdn.billing.events.sent", 
            "customer", customerId).increment();
        meterRegistry.gauge("cdn.billing.latest.usage.gb", 
            Tags.of("customer", customerId), usageGb);
    }
    
    public void recordBillingJobExecution(int processed, int errors, Duration duration) {
        meterRegistry.counter("cdn.billing.job.executions").increment();
        meterRegistry.counter("cdn.billing.job.customers.processed").increment(processed);
        meterRegistry.counter("cdn.billing.job.errors").increment(errors);
        meterRegistry.timer("cdn.billing.job.duration").record(duration);
    }
    
    public void recordCappingEvent(String reason, String distributionId, Double usageGb) {
        meterRegistry.counter("cdn.capping.events", 
            "reason", reason,
            "distribution", distributionId).increment();
        meterRegistry.gauge("cdn.capping.usage.gb",
            Tags.of("distribution", distributionId), usageGb);
    }
    
    public void recordDataCollection(String source, int snapshotCount) {
        meterRegistry.counter("cdn.data.collection.snapshots",
            "source", source).increment(snapshotCount);
    }
}
