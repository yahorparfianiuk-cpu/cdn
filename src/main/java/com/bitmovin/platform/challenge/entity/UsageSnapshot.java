package com.bitmovin.platform.challenge.entity;

import com.bitmovin.platform.challenge.entity.enums.UsageDataSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "usage_snapshot", indexes = {
        @Index(name = "idx_customer_time", columnList = "customer_id,snapshot_time"),
        @Index(name = "idx_dist_time_source", columnList = "distribution_id,snapshot_time,source")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UsageSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distribution_id")
    private String distributionId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "snapshot_time")
    private Instant snapshotTime;

    @Column(name = "data_transfer_gb")
    private Double dataTransferGb;

    @Enumerated(EnumType.STRING)
    private UsageDataSource source; // CLOUDWATCH, COST_EXPLORER

    @Column(name = "period_start")
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;
}
