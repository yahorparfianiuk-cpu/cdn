package com.bitmovin.platform.challenge.application;

import com.bitmovin.platform.challenge.domain.DailyEntity;
import com.bitmovin.platform.challenge.domain.DailyUsagePerCustomer;
import com.bitmovin.platform.challenge.domain.repo.CustomerUsageRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Setter
@Getter
public class DailyUsageService {

    private final CustomerUsageRepository usageRepo;

    public DailyUsageService(CustomerUsageRepository usageRepo) {
        this.usageRepo = usageRepo;
    }

    public List<DailyEntity> getDailyUsage(String customerId, LocalDate from, LocalDate to) {
        return usageRepo.findByCustomerIdAndDateBetween(customerId, from, to);
    }

    public void saveUsage(DailyEntity usage) {
        usageRepo.save(usage);
    }

}
