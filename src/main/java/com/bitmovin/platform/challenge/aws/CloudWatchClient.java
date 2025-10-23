package com.bitmovin.platform.challenge.aws;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;

public class CloudWatchClient {

  private final List<RecordedMetricUsage> mockedUsage;

  public CloudWatchClient() {
    this(List.of(
        new RecordedMetricUsage(UUID.randomUUID().toString(), 50.4d),
        new RecordedMetricUsage(UUID.randomUUID().toString(), 0.076d),
        new RecordedMetricUsage(UUID.randomUUID().toString(), 70.076d)
        ));
  }

  public CloudWatchClient(List<RecordedMetricUsage> usage) {
    this.mockedUsage = usage;
  }

  public GetMetricDataResponse getMetrics(Instant from, Instant to) {
    var metricData = mockedUsage.stream()
        .map(u -> MetricDataResult.builder()
            .id("q1")
            .label("Distribution "+ u.distribution)
            .values(u.usage)
            .build())
        .toList();

    return GetMetricDataResponse.builder()
        .metricDataResults(metricData)
        .build();
  }

  public record RecordedMetricUsage(String distribution, Double usage) {}
}
