package com.example.demo.fs.impl;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.example.demo.core.util.FileUtils;
import com.example.demo.fs.FileStorage;

import lombok.Getter;

public abstract class AbstractFileStorage implements FileStorage, BeanNameAware {

	@Value("${fileStorage.baseUrl:}")
	protected String baseUrl;

	@Getter
	private String name;

	@Override
	public String getFileUrl(String path) {
		path = FileUtils.normalizePath(path);
		if (!path.startsWith("/"))
			path = '/' + path;
		return StringUtils.hasLength(baseUrl) ? baseUrl + path : path;
	}

	@Override
	public void setBeanName(String beanName) {
		if (beanName.equalsIgnoreCase("FileStorage")) {
			name = FileStorage.super.getName();
		} else {
			if (beanName.endsWith("FileStorage"))
				beanName = beanName.substring(0, beanName.length() - "FileStorage".length());
			name = beanName;
		}
	}

	protected String trimTailSlash(String input) {
		if (!StringUtils.hasLength(input) || !input.endsWith("/"))
			return input;
		return input.substring(0, input.length() - 1);
	}

}
