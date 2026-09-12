package com.finance.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncExecutorConfig {

    @Bean(name = "llmExecutor")
    public ExecutorService llmExecutor() {
        return Executors.newFixedThreadPool(20);
    }
}