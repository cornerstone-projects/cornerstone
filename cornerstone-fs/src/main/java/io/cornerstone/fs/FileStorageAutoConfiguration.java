package io.cornerstone.fs;

import java.net.URI;

import io.cornerstone.fs.impl.FtpFileStorage;
import io.cornerstone.fs.impl.LocalFileStorage;
import io.cornerstone.fs.impl.S3FileStorage;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public FileStorage fileStorage(FileStorageProperties fileStorageProperties) {
		URI uri = fileStorageProperties.getUri();
		if (uri != null) {
			String scheme = uri.getScheme();
			return ((scheme != null) && scheme.startsWith("ftp")) ? new FtpFileStorage(fileStorageProperties)
					: new LocalFileStorage(fileStorageProperties);
		}
		if (fileStorageProperties.getS3().getAccessKey() != null) {
			return new S3FileStorage(fileStorageProperties);
		}
		return null;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnBean(ObservationRegistry.class)
	public FileStorageInstrumentation fileStorageInstrumentation(ObservationRegistry observationRegistry) {
		return new FileStorageInstrumentation(observationRegistry);
	}

}
