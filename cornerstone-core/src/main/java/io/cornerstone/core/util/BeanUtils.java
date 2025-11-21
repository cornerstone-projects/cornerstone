package io.cornerstone.core.util;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Id;
import lombok.experimental.UtilityClass;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

@UtilityClass
public class BeanUtils {

	public static void copyNonNullProperties(Object source, Object target, String... ignoreProperties) {
		BeanWrapper bw = new BeanWrapperImpl(source);
		Set<String> ignores = Stream.of(bw.getPropertyDescriptors())
			.map(FeatureDescriptor::getName)
			.filter(name -> bw.getPropertyValue(name) == null)
			.collect(Collectors.toSet());
		if (ignoreProperties.length > 0) {
			ignores.addAll(List.of(ignoreProperties));
		}
		org.springframework.beans.BeanUtils.copyProperties(source, target, ignores.toArray(new String[0]));
	}

	public static void copyPropertiesInJsonView(Object source, Object target, Class<?> view) {
		BeanWrapper sourceBW = new BeanWrapperImpl(source);
		BeanWrapper targetBW = new BeanWrapperImpl(target);
		for (PropertyDescriptor pd : sourceBW.getPropertyDescriptors()) {
			String name = pd.getName();
			Method m = pd.getReadMethod();
			if (m == null) {
				continue;
			}
			if (findAnnotation(m, name, Id.class) != null) {
				continue;
			}
			JsonView jsonView = findAnnotation(m, name, JsonView.class);
			if (jsonView == null) {
				continue;
			}
			JsonProperty jsonProperty = findAnnotation(m, name, JsonProperty.class);
			if ((jsonProperty != null) && (jsonProperty.access() == Access.READ_ONLY)) {
				continue;
			}
			for (Class<?> clazz : jsonView.value()) {
				if (clazz.isAssignableFrom(view)) {
					targetBW.setPropertyValue(name, sourceBW.getPropertyValue(name));
					break;
				}
			}

		}

	}

	private static <T extends Annotation> T findAnnotation(Method getter, String propertyName, Class<T> clazz) {
		T anno = getter.getAnnotation(clazz);
		if (anno == null) {
			try {
				anno = getter.getDeclaringClass().getDeclaredField(propertyName).getAnnotation(clazz);
			}
			catch (Exception ex) {
			}
		}
		return anno;
	}

}
