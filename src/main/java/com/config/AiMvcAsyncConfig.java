package com.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AiMvcAsyncConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AiMvcAsyncConfig.class);
    private static final long ASYNC_TIMEOUT_MILLIS = 120000L;

    private final ThreadPoolTaskExecutor aiMvcAsyncExecutor;

    public AiMvcAsyncConfig(@Qualifier("aiMvcAsyncExecutor") ThreadPoolTaskExecutor aiMvcAsyncExecutor) {
        this.aiMvcAsyncExecutor = aiMvcAsyncExecutor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(aiMvcAsyncExecutor);
        configurer.setDefaultTimeout(ASYNC_TIMEOUT_MILLIS);
        logger.info("Configured MVC async support executorBean=aiMvcAsyncExecutor threadPrefix={} timeoutMillis={}",
                aiMvcAsyncExecutor.getThreadNamePrefix(), ASYNC_TIMEOUT_MILLIS);
    }
}

@Configuration
class AiMvcAsyncExecutorConfig {

    private static final Logger logger = LoggerFactory.getLogger(AiMvcAsyncExecutorConfig.class);

    @Bean(name = "aiMvcAsyncExecutor")
    public ThreadPoolTaskExecutor aiMvcAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-sse-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        logger.info("Initialized MVC async executor bean=aiMvcAsyncExecutor threadPrefix={} corePoolSize={} maxPoolSize={} queueCapacity={}",
                executor.getThreadNamePrefix(), executor.getCorePoolSize(), executor.getMaxPoolSize(), 100);
        return executor;
    }
}
