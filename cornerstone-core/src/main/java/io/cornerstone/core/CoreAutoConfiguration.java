package io.cornerstone.core;

import io.cornerstone.core.persistence.convert.AbstractArrayConverter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableRetry(order = -100) // keep it because framework's @Retryable doesn't support
							// recover
@EnableResilientMethods(order = -99)
@EnableTransactionManagement(order = 0, proxyTargetClass = true)
@AutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
@EntityScan(basePackageClasses = AbstractArrayConverter.class)
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
public class CoreAutoConfiguration {

}
