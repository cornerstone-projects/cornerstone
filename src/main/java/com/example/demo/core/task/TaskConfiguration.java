package com.example.demo.core.task;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.boot.task.TaskSchedulerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import com.example.demo.core.util.RunnableWithRequestId;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;

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
	@ConditionalOnBean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
	public ThreadPoolTaskScheduler taskScheduler(TaskSchedulerBuilder builder) {
		// RedisHttpSessionConfiguration.SessionCleanupConfiguration
		// implements SchedulingConfigurer
		// cause not created by TaskSchedulingAutoConfiguration
		return builder.build();
	}

	@Bean(name = { TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME,
			AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME })
	public ThreadPoolTaskExecutor taskExecutor(TaskExecutorBuilder builder) {
		// taskScheduler implements Executor
		// cause not created by TaskExecutionAutoConfiguration
		return builder.build();
	}

	@Bean
	TaskSchedulerCustomizer taskSchedulerCustomizer() {
		return taskScheduler -> taskScheduler.setErrorHandler(ex -> {
			if (ex instanceof BulkheadFullException || ex instanceof RequestNotPermitted)
				log.warn("Error occurred in scheduled task: {}", ex.getLocalizedMessage());
			else
				log.error("Unexpected error occurred in scheduled task", ex);
		});
	}

}
