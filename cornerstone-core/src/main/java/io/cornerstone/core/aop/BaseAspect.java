package io.cornerstone.core.aop;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class BaseAspect implements Ordered {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Getter
	@Setter
	protected int order;

	protected boolean isBypass() {
		return AopContext.isBypass(this.getClass());
	}

	protected String buildKey(ProceedingJoinPoint jp) {
		StringBuilder sb = new StringBuilder();
		Class<?> beanClass = jp.getTarget().getClass();
		String beanName = buildDefaultBeanName(beanClass.getName());
		Component comp = AnnotatedElementUtils.getMergedAnnotation(beanClass, Component.class);
		if ((comp != null) && StringUtils.hasLength(comp.value())) {
			beanName = comp.value();
		}
		MethodSignature signature = (MethodSignature) jp.getSignature();
		sb.append(beanName).append('.').append(signature.getName()).append('(');
		Class<?>[] parameterTypes = signature.getParameterTypes();
		if (parameterTypes.length > 0) {
			for (int i = 0; i < parameterTypes.length; i++) {
				sb.append(parameterTypes[i].getSimpleName());
				if (i != (parameterTypes.length - 1)) {
					sb.append(',');
				}
			}
		}
		sb.append(')');
		return sb.toString();
	}

	static String buildDefaultBeanName(String beanClassName) {
		String shortClassName = ClassUtils.getShortName(beanClassName);
		String parentPackage = BaseAspect.class.getPackageName();
		parentPackage = parentPackage.substring(0, parentPackage.lastIndexOf('.'));
		if (beanClassName.startsWith(parentPackage)) {
			if (shortClassName.startsWith("Default") && (shortClassName.length() > 7)) {
				return StringUtils.uncapitalize(shortClassName.substring(7));
			}
			if (beanClassName.endsWith("Configuration")) {
				try {
					Class<?> clazz = Class.forName(beanClassName);
					if (clazz.isAnnotationPresent(Configuration.class)) {
						Role role = clazz.getAnnotation(Role.class);
						if ((role != null) && (role.value() == BeanDefinition.ROLE_INFRASTRUCTURE)) {
							return beanClassName;
						}
					}
				}
				catch (ClassNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		}
		if (shortClassName.endsWith("Impl") && (shortClassName.length() > 4)) {
			shortClassName = shortClassName.substring(0, shortClassName.length() - 4);
		}
		return StringUtils.uncapitalize(shortClassName);
	}

}
