package com.example.demo.core.scheduling;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import com.example.demo.core.util.RunnableWithRequestId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(0)
@EnableScheduling
@EnableAsync(order = -999, proxyTargetClass = true)
@Configuration(proxyBeanMethods = false)
public class SchedulingConfiguration {

	@Bean
	TaskDecorator taskDecorator() {
		return RunnableWithRequestId::new;
	}

	@Bean
	@ConditionalOnBean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
	public ThreadPoolTaskScheduler taskScheduler(TaskSchedulerBuilder builder) {
		// RedisHttpSessionConfiguration.SessionCleanupConfiguration
		// cause not created by TaskSchedulingAutoConfiguration
		return builder.build();
	}

	@Bean
	TaskSchedulerCustomizer taskSchedulerCustomizer() {
		return taskScheduler -> taskScheduler.setErrorHandler(ex -> {
			String className = ex.getClass().getName();
			if (className.equals("io.github.resilience4j.bulkhead.BulkheadFullException")
					|| className.equals("io.github.resilience4j.ratelimiter.RequestNotPermitted"))
				log.warn("Error occurred in scheduled task: {}", ex.getLocalizedMessage());
			else
				log.error("Unexpected error occurred in scheduled task", ex);
		});
	}

}
