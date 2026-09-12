package com.finance.ai.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfigBeans {

    @Bean
    public RateLimiterConfig chatClientRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ZERO) // fail fast, no queueing
                .build();
    }

    @Bean
    public RateLimiterConfig chatAgentClientRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(3)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ZERO)
                .build();
    }
}