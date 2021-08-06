package io.cornerstone.core;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.cornerstone.core.hibernate.convert.AbstractArrayConverter;

@Configuration(proxyBeanMethods = false)
@ComponentScan
@EntityScan(basePackageClasses = AbstractArrayConverter.class)
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
public class CoreAutoConfiguration {

}
