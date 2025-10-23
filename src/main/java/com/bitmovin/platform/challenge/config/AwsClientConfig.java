package com.bitmovin.platform.challenge.config;

import com.bitmovin.platform.challenge.aws.CloudWatchClient;
import com.bitmovin.platform.challenge.aws.CostExplorerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsClientConfig {
    
    @Bean
    public CloudWatchClient cloudWatchClient() {
        return new CloudWatchClient();
    }
    
    @Bean
    public CostExplorerClient costExplorerClient() {
        return new CostExplorerClient();
    }
}
