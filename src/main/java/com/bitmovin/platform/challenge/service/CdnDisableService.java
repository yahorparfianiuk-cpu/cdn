package com.bitmovin.platform.challenge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class CdnDisableService {
    
    /**
     * Disable CDN distribution due to usage limit exceeded
     * In production: Would call AWS CloudFront API to disable distribution
     * For challenge: Mock with event publication
     */
    public void disableDistribution(String distributionId, String reason, Double usageGb) {
        String event = formatDisableEvent(distributionId, reason, usageGb);
        
        // Mock: Print to console
        System.out.println("=====================================================");
        System.out.println("CDN DISABLE EVENT");
        System.out.println("=====================================================");
        System.out.println(event);
        System.out.println("=====================================================");
        
        log.warn("CDN distribution {} disabled: {}", distributionId, reason);
        
        // In production:
        // 1. Call AWS CloudFront to disable distribution
        // cloudFrontClient.updateDistribution(distributionId, enabled=false);
        // 2. Publish event to message broker
        // kafkaTemplate.send("cdn-events", distributionId, event);
    }
    
    private String formatDisableEvent(String distributionId, String reason, Double usageGb) {
        return String.format("""
            {
              "type": "EVENT_CDN_DISABLED",
              "payload": {
                "distributionId": "%s",
                "reason": "%s",
                "usageGb": "%.2f",
                "timestamp": "%s"
              }
            }
            """,
            distributionId,
            reason,
            usageGb,
            Instant.now().toString()
        );
    }
}
