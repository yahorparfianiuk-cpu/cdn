package com.bitmovin.platform.challenge.entity;

import com.bitmovin.platform.challenge.entity.enums.BillingEventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "billing_event", uniqueConstraints = {
    @UniqueConstraint(name = "uk_billing", 
                     columnNames = {"customer_id", "period_start", "period_end"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BillingEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Column(name = "period_start", nullable = false)
    private Instant periodStart;
    
    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;
    
    @Column(name = "traffic_usage_gb", nullable = false)
    private Double trafficUsageGb;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingEventStatus status;
}
