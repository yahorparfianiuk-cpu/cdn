package com.bitmovin.platform.challenge.service;

import com.bitmovin.platform.challenge.repository.UsageSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UsageSnapshotService {

    private final UsageSnapshotRepository usageSnapshotRepository;



}
