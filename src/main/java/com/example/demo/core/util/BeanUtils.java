package com.example.demo.core.util;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BeanUtils {

	public static void copyNonNullProperties(Object source, Object target, String... ignoreProperties) {
		BeanWrapper bw = new BeanWrapperImpl(source);
		Set<String> ignores = new HashSet<>(Stream.of(bw.getPropertyDescriptors()).map(FeatureDescriptor::getName)
				.filter(name -> bw.getPropertyValue(name) == null).collect(Collectors.toSet()));
		if (ignoreProperties.length > 0)
			ignores.addAll(Arrays.asList(ignoreProperties));
		org.springframework.beans.BeanUtils.copyProperties(source, target, ignores.toArray(new String[ignores.size()]));
	}

	public static void copyPropertiesInJsonView(Object source, Object target, Class<?> view) {
		BeanWrapper sourceBW = new BeanWrapperImpl(source);
		BeanWrapper targetBW = new BeanWrapperImpl(target);
		for (PropertyDescriptor pd : sourceBW.getPropertyDescriptors()) {
			String name = pd.getName();
			Method m = pd.getReadMethod();
			if (m == null)
				continue;
			JsonView jsonView = m.getAnnotation(JsonView.class);
			if (jsonView == null) {
				try {
					jsonView = m.getDeclaringClass().getDeclaredField(name).getAnnotation(JsonView.class);
				} catch (Exception e) {
				}
			}
			if (jsonView == null)
				continue;
			for (Class<?> clazz : jsonView.value()) {
				if (clazz.isAssignableFrom(view)) {
					targetBW.setPropertyValue(name, sourceBW.getPropertyValue(name));
					break;
				}
			}

		}

	}
}
