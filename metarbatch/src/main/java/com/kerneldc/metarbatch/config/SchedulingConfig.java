package com.kerneldc.metarbatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final int POOL_SIZE = 3;
    private static final String THREAD_NAME_PREFIX = "scheduled-task-pool-";
    
    /**
     * Configures the scheduler to allow multiple threads.
     *
     * @param taskRegistrar The task register.
     */
	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

		threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
		threadPoolTaskScheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
		threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true); // Wait for tasks on shutdown
        return threadPoolTaskScheduler;
    }

}
