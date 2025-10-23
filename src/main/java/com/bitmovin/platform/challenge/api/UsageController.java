package com.bitmovin.platform.challenge.api;

import com.bitmovin.platform.challenge.application.UsageService;
import com.bitmovin.platform.challenge.domain.DailyEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/usage")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping
    public ResponseEntity<List<DailyEntity>> getUsage(
            @RequestHeader("X-Customer-Id") String customerId,
            @RequestParam Instant from,
            @RequestParam Instant to) {

        List<DailyEntity> data = usageService.getUsage(customerId, from, to);
        return ResponseEntity.ok(data);
    }
}
