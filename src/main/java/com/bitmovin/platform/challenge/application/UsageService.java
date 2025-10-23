package com.bitmovin.platform.challenge.application;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import com.bitmovin.platform.challenge.infrastructure.aws.CloudWatchClient;
import com.bitmovin.platform.challenge.infrastructure.aws.CostExplorerClient;
import com.bitmovin.platform.challenge.domain.repo.UsageRepository2;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class UsageService {

    private final CloudWatchClient cloudWatchClient;
    private final CostExplorerClient costExplorerClient;
    private final UsageRepository2 usageRepository;
//
    public UsageService(CloudWatchClient cloudWatchClient,
                        CostExplorerClient costExplorerClient,
                        UsageRepository usageRepository) {
        this.cloudWatchClient = cloudWatchClient;
        this.costExplorerClient = costExplorerClient;
        this.usageRepository = usageRepository;
    }
//
    @Scheduled(fixedRate = 15 * 60 * 1000)
    public void collectShortTermUsage() {
        var response = cloudWatchClient.getMetrics(
                Instant.now().minus(Duration.ofMinutes(15)), Instant.now());
        response.metricDataResults().forEach(m ->
                usageRepository.save(
                        new DailyEntity(m.label(), Instant.now(),
                                m.values().stream()
                                        .mapToDouble(Double::doubleValue)
                                        .sum()))
        );
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void collectDailyUsage() {
        var now = Instant.now();
        var response = costExplorerClient.retrieveUsage(
                now.minus(Duration.ofDays(1)), now, "some-distribution");
        // здесь можно разобрать и сохранить usage для биллинга
    }

    // Получить usage для API
    public List<DailyEntity> getUsage(String customerId, Instant from, Instant to) {
        return usageRepository.findByCustomerAndPeriod(customerId, from, to);
    }

}
