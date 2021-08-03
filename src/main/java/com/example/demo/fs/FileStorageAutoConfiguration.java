package com.example.demo.fs;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.fs.impl.FtpFileStorage;
import com.example.demo.fs.impl.LocalFileStorage;
import com.example.demo.fs.impl.S3FileStorage;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public FileStorage fileStorage(FileStorageProperties fileStorageProperties) {
		URI uri = fileStorageProperties.getUri();
		if (uri != null) {
			String scheme = uri.getScheme();
			return (scheme != null && scheme.startsWith("ftp")) ? new FtpFileStorage(fileStorageProperties)
					: new LocalFileStorage(fileStorageProperties);
		}
		if (fileStorageProperties.getS3().getAccessKey() != null)
			return new S3FileStorage(fileStorageProperties);
		return null;
	}

}