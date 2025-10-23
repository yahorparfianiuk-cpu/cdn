package com.bitmovin.platform.challenge.aws;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.services.costexplorer.model.DateInterval;
import software.amazon.awssdk.services.costexplorer.model.GetCostAndUsageResponse;
import software.amazon.awssdk.services.costexplorer.model.Group;
import software.amazon.awssdk.services.costexplorer.model.MetricValue;
import software.amazon.awssdk.services.costexplorer.model.ResultByTime;

public class CostExplorerClient {

  private final List<RecordedBillingUsage> mockedUsage;

  public CostExplorerClient() {
    this(List.of(
        new RecordedBillingUsage(UUID.randomUUID().toString(), 50.4d, 300.67d),
        new RecordedBillingUsage(UUID.randomUUID().toString(), 0.076d, 2.345d),
        new RecordedBillingUsage(UUID.randomUUID().toString(), 70.076d, 0.084d)
    ));
  }

  public CostExplorerClient(List<RecordedBillingUsage> mockedUsage) {
    this.mockedUsage = mockedUsage;
  }

  public GetCostAndUsageResponse retrieveUsage(Instant from, Instant to, String distribution) {
    var maybeUsage = mockedUsage.stream().filter(x -> x.distribution.equals(distribution)).findFirst();

    if(maybeUsage.isEmpty()) {
      return build(from, to, Map.of());
    }

    var usage = maybeUsage.get();
    return build(from, to, Map.of("-TimedStorage-ByteHrs", usage.diskUsage, "-DataTransfer-Out-Bytes", usage.dataTransferUsage));
  }

  private GetCostAndUsageResponse build(Instant from, Instant to, Map<String, Double> usage) {
    var groups = usage.entrySet().stream()
        .map(entry ->
          Group.builder()
              .keys(entry.getKey())
              .metrics(Map.of("UsageQuantity", MetricValue.builder().amount(entry.getValue().toString()).build()))
              .build()
        ).toList();

    return GetCostAndUsageResponse.builder()
        .resultsByTime(
            ResultByTime.builder()
                .timePeriod(
                    DateInterval.builder()
                        .start(from.toString())
                        .end(to.toString())
                        .build()
                )
                .groups(groups)
                .build()
        )
        .build();

  }

  public record RecordedBillingUsage(String distribution, Double diskUsage, Double dataTransferUsage) {}
}
