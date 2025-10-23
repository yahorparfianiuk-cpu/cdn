package com.bitmovin.platform.challenge.infrastructure.aws.config;

import com.bitmovin.platform.challenge.infrastructure.aws.CloudWatchClient;
import com.bitmovin.platform.challenge.infrastructure.aws.CostExplorerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsMockConfig {

    @Bean
    public CloudWatchClient cloudWatchClient() {
        return new CloudWatchClient();
    }

    @Bean
    public CostExplorerClient costExplorerClient() {
        return new CostExplorerClient();
    }
}
