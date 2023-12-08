package io.cornerstone.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

public class I18N {

	private static final Map<Class<?>, ResourceBundleMessageSource> cache = new ConcurrentHashMap<>();

	public static String getMessage(Class<?> baseClass, String code) {
		ResourceBundleMessageSource messageSource = cache.get(baseClass);
		if (messageSource == null) {
			messageSource = cache.computeIfAbsent(baseClass, k -> {
				ResourceBundleMessageSource temp = new ResourceBundleMessageSource();
				temp.setBundleClassLoader(baseClass.getClassLoader());
				temp.setBasename(baseClass.getName());
				return temp;
			});
		}
		return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

}
