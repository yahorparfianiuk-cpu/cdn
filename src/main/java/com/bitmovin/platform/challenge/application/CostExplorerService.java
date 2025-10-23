package com.bitmovin.platform.challenge.application;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import com.bitmovin.platform.challenge.domain.Distribution;
import com.bitmovin.platform.challenge.domain.repo.DistributionRepository;
import com.bitmovin.platform.challenge.infrastructure.aws.CostExplorerClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class CostExplorerService {

    private final CostExplorerClient costExplorerClient;
    private final UsageService usageService;
    private final BillingService billingService;
    private final DistributionRepository distributionRepo;

    public CostExplorerService(CostExplorerClient costExplorerClient, UsageService usageService,
                               BillingService billingService, DistributionRepository distributionRepo) {
        this.costExplorerClient = costExplorerClient;
        this.usageService = usageService;
        this.billingService = billingService;
        this.distributionRepo = distributionRepo;
    }

    public void retrieveAndSendDailyUsage() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        for (Distribution dist : distributionRepo.findAll()) {
            var response = costExplorerClient.retrieveUsage(
                    yesterday.atStartOfDay().toInstant(ZoneOffset.UTC),
                    yesterday.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                    dist.getId()
            );

            // Sum data transfer usage from Cost Explorer response
            BigDecimal dataTransferGb = response.resultsByTime().get(0).groups().stream()
                    .filter(g -> g.keys().contains("-DataTransfer-Out-Bytes"))
                    .map(g -> new BigDecimal(g.metrics().get("UsageQuantity").amount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Create DailyEntity with correct customerId and distributionId
            DailyEntity usage = new DailyEntity(
                    dist.getCustomerId(),   // customerId
                    dist.getId(),           // distributionId
                    yesterday,              // date
                    dataTransferGb          // usage in GB
            );

            usageService.saveUsage(usage);
            billingService.sendBillingUsage(usage);
        }
    }
}
