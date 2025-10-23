package com.bitmovin.platform.challenge.service;

import com.bitmovin.platform.challenge.repository.DistributionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DistributionService {

    private final DistributionRepository distributionRepository;
}
