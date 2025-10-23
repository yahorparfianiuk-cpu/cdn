package com.bitmovin.platform.challenge.infrastructure.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UsageEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public UsageEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

















    public void publishUsage(String customerId, double usageGb) {
        String message = String.format("{\"customerId\":\"%s\",\"usageGb\":%s}", customerId, usageGb);
        kafkaTemplate.send("usage-events", customerId, message);
        System.out.println("Published usage event: " + message);
    }
}
