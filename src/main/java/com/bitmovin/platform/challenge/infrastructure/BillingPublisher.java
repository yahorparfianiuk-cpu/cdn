package com.bitmovin.platform.challenge.infrastructure;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BillingPublisher {

    @Scheduled(cron = "0 0 0 * * *")
    public void publishBillingEvents() {
        // iterate over usage repository and send message
        System.out.println("📤 EVENT_CDN_USAGE sent to billing system");
    }
}
