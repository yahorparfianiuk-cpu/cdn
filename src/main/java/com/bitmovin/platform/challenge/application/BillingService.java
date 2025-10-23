package com.bitmovin.platform.challenge.application;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    public void sendBillingUsage(DailyEntity usage) {
        // kafka Publish EVENT_CDN_USAGE
        System.out.println("Billing Event: " + usage);
    }
}
