package io.cornerstone.fs;

import java.net.URI;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("file-storage")
@Data
public class FileStorageProperties {

	private URI uri;

	private Ftp ftp = new Ftp();

	private S3 s3 = new S3();

	@Data
	public static class Ftp {

		private String workingDirectory;

		private int defaultTimeout = 10000;

		private int dataTimeout = 10000;

		private String controlEncoding = "UTF-8";

		private boolean binaryMode = true;

		private boolean passiveMode = true;

		private long bufferThreshold = 1048576;

		private boolean useLocaltime;

		private Pool pool = new Pool();

	}

	@Data
	public static class S3 {

		private String accessKey;

		private String secretKey;

		private String region;

		private String endpoint;

		private boolean useHttps;

		private String bucket;

		private String domain;

		private boolean pathStyleAccess = true;

		private int batchSize = 100;

		private boolean hierarchicalDirectory;

	}

	@Data
	public static class Pool {

		private int maxTotal = 20;

		private int maxIdle = 5;

		private int minIdle = 1;

		private int maxWaitMillis = 60000;

		private int minEvictableIdleTimeMillis = 300000;

	}

}
