package io.cornerstone.core.task;

import io.cornerstone.core.coordination.LockFailedException;
import io.cornerstone.core.util.RunnableWithRequestId;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Order(0)
@EnableScheduling
@EnableAsync(order = -999, proxyTargetClass = true)
@Configuration(proxyBeanMethods = false)
public class TaskConfiguration {

	@Bean
	TaskDecorator taskDecorator() {
		return RunnableWithRequestId::new;
	}

	@Bean
	TaskSchedulerCustomizer taskSchedulerCustomizer() {
		return taskScheduler -> taskScheduler.setErrorHandler(ex -> {
			if ((ex instanceof LockFailedException) || (ex instanceof BulkheadFullException)
					|| (ex instanceof RequestNotPermitted)) {
				log.warn("Error occurred in scheduled task: {}", ex.getLocalizedMessage());
			}
			else {
				log.error("Unexpected error occurred in scheduled task", ex);
			}
		});
	}

}
