package com.example.demo.core.util;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

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

}
