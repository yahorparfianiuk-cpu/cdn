//package com.bitmovin.platform.challenge.domain;
//
//import org.springframework.stereotype.Repository;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Repository
//public class UsageRepository {
//    private final Map<String, List<UsageEntity>> usageData = new ConcurrentHashMap<>();
//
//    public void save(UsageEntity usage) {
//        usageData.computeIfAbsent(usage.customerId(), k -> new ArrayList<>()).add(usage);
//    }
//
//    public List<UsageEntity> findByCustomerAndPeriod(String customerId, Instant from, Instant to) {
//        return usageData.getOrDefault(customerId, List.of()).stream()
//                .filter(u -> !u.timestamp().isBefore(from) && !u.timestamp().isAfter(to))
//                .toList();
//    }
//}
//
