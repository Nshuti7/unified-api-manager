package com.unifiedapi.unifiedapimanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "webhookExecutor")
    public Executor webhookExecutor(@Value("${webhook.thread-pool.size:10}") int size,
                                    @Value("${webhook.thread-pool.queue:100}") int queue) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(size);
        executor.setMaxPoolSize(size * 2);
        executor.setQueueCapacity(queue);
        executor.setThreadNamePrefix("webhook-");
        executor.initialize();
        return executor;
    }
}
