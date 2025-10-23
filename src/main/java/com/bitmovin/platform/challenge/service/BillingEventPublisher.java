package com.bitmovin.platform.challenge.service;

import com.bitmovin.platform.challenge.entity.BillingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillingEventPublisher {
    
    /**
     * Publish billing event to billing system
     * In production: Would use Kafka/RabbitMQ
     * For challenge: Print to console
     */
    public void publishBillingEvent(BillingEvent event) {
        String json = formatBillingEvent(event);
        
        // Mock: Print to console
        System.out.println("=====================================================");
        System.out.println("BILLING EVENT PUBLISHED");
        System.out.println("=====================================================");
        System.out.println(json);
        System.out.println("=====================================================");
        
        log.info("Published billing event for customer {} (period {} to {})", 
                event.getCustomerId(), event.getPeriodStart(), event.getPeriodEnd());
        
        // In production:
        // kafkaTemplate.send("billing-events", event.getCustomerId(), json);
    }
    
    private String formatBillingEvent(BillingEvent event) {
        return String.format("""
            {
              "type": "EVENT_CDN_USAGE",
              "payload": {
                "id": "%s",
                "customerId": "%s",
                "startPeriod": "%s",
                "endPeriod": "%s",
                "trafficUsageGb": "%.2f"
              }
            }
            """,
            event.getId(),
            event.getCustomerId(),
            event.getPeriodStart().toString(),
            event.getPeriodEnd().toString(),
            event.getTrafficUsageGb()
        );
    }
}
