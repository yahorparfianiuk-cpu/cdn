package com.bitmovin.platform.challenge.entity;

import com.bitmovin.platform.challenge.entity.enums.DistributionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "distribution")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Distribution {

    @Id
    @Column(name = "distribution_id")
    private String distributionId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "bucket_id")
    private String bucketId;

    @Enumerated(EnumType.STRING)
    private DistributionStatus status; // ACTIVE, DISABLED

    @Column(name = "disabled_at")
    private Instant disabledAt;

    @Column(name = "disable_reason")
    private String disableReason;

    @Version
    private Long version; // Optimistic locking for HA
}
