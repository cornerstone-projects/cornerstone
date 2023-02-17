package io.cornerstone.core.security;

import java.lang.reflect.Method;
import java.util.Collection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.annotation.SecuredAnnotationSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("deprecation")
@Configuration(proxyBeanMethods = false)
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnProperty(name = "security.protecting", havingValue = "true", matchIfMissing = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

	@Override
	protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
		// override @EnableGlobalMethodSecurity(securedEnabled=true)
		return new SecuredAnnotationSecurityMetadataSource() {

			@Override
			public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
				Collection<ConfigAttribute> attr = super.getAttributes(method, targetClass);
				if (CollectionUtils.isEmpty(attr)) {
					attr = findAttributes(targetClass);
				}
				return attr;
			}

		};
	}

}
