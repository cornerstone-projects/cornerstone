package io.cornerstone.fs.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.http.IdleConnectionReaper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.cornerstone.fs.FileInfo;
import io.cornerstone.fs.FileStorageProperties;
import io.cornerstone.fs.FileStorageProperties.S3;
import io.cornerstone.fs.Paged;

import org.springframework.util.StringUtils;

public class S3FileStorage extends BucketFileStorage {

	private static final String NO_SUCH_KEY = "NoSuchKey";

	private static final String NOT_FOUND = "404 Not Found";

	private final S3 config;

	protected AmazonS3 s3;

	public S3FileStorage(FileStorageProperties fileStorageProperties) {
		this.config = fileStorageProperties.getS3();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		AWSCredentials credentials = new BasicAWSCredentials(this.config.getAccessKey(), this.config.getSecretKey());
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setProtocol(this.config.isUseHttps() ? Protocol.HTTP : Protocol.HTTPS);
		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withClientConfiguration(clientConfig);
		if (StringUtils.hasText(this.config.getEndpoint())) {
			builder.withPathStyleAccessEnabled(this.config.isPathStyleAccess()).withEndpointConfiguration(
					new EndpointConfiguration(this.config.getEndpoint(), this.config.getRegion()));
		}
		else {
			builder.withRegion(this.config.getRegion());
		}
		this.s3 = builder.build();
		if (!this.s3.doesBucketExistV2(this.config.getBucket())) {
			this.s3.createBucket(this.config.getBucket());
		}
	}

	@PreDestroy
	public void destroy() {
		IdleConnectionReaper.shutdown();
	}

	@Override
	public String getBucket() {
		return this.config.getBucket();
	}

	@Override
	public boolean isUseHttps() {
		return this.config.isUseHttps();
	}

	@Override
	public String getDomain() {
		return this.config.getDomain();
	}

	@Override
	protected boolean isHierarchicalDirectory() {
		return this.config.isHierarchicalDirectory();
	}

	@Override
	public void doWrite(InputStream is, String path, long contentLength, String contentType) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		if (contentLength > -1) {
			metadata.setContentLength(contentLength);
		}
		if (contentType != null) {
			metadata.setContentType(contentType);
		}
		this.s3.putObject(getBucket(), path, is, metadata);
	}

	@Override
	public InputStream open(String path) throws IOException {
		path = normalizePath(path);
		try {
			S3Object object = this.s3.getObject(getBucket(), path);
			if (object.getObjectMetadata().getContentLength() == 0) {
				return null;
			}
			return object.getObjectContent();
		}
		catch (AmazonS3Exception ex) {
			if (NO_SUCH_KEY.equals(ex.getErrorCode())) {
				return null;
			}
			throw ex;
		}
	}

	@Override
	protected boolean doMkdir(String path) {
		if (path.equals("") || path.equals("/")) {
			return true;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		path = normalizePath(path);
		try {
			return this.s3.getObjectMetadata(getBucket(), path).getContentLength() == 0;
		}
		catch (AmazonS3Exception ex) {
			if (!NOT_FOUND.equals(ex.getErrorCode())) {
				throw ex;
			}
		}
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(0);
		metadata.setLastModified(new Date());
		this.s3.putObject(getBucket(), path, new ByteArrayInputStream(new byte[0]), metadata);
		return true;
	}

	@Override
	public boolean delete(String path) {
		if (path.equals("") || path.equals("/")) {
			return false;
		}
		path = normalizePath(path);
		if ((path.lastIndexOf('.') <= path.lastIndexOf('/')) && isDirectory(path)) {
			if (!path.endsWith("/")) {
				path += "/";
			}
			if (!listFilesAndDirectory(path, 1, null).getResult().isEmpty()) {
				return false;
			}
			this.s3.deleteObject(getBucket(), path);
			return true;
		}
		else {
			this.s3.deleteObject(getBucket(), path);
			return true;
		}
	}

	@Override
	public long getLastModified(String path) {
		if (path.equals("") || path.equals("/")) {
			return 0;
		}
		path = normalizePath(path);
		try {
			return this.s3.getObjectMetadata(getBucket(), path).getLastModified().getTime();
		}
		catch (AmazonS3Exception ex) {
			if (NOT_FOUND.equals(ex.getErrorCode())) {
				return 0;
			}
			throw ex;
		}
	}

	@Override
	public boolean exists(String path) {
		if (path.equals("") || path.equals("/")) {
			return true;
		}
		if (path.endsWith("/")) {
			return isDirectory(path);
		}
		path = normalizePath(path);
		return this.s3.doesObjectExist(getBucket(), path);
	}

	@Override
	public boolean isDirectory(String path) {
		if (path.equals("") || path.equals("/")) {
			return true;
		}
		if (!path.endsWith("/")) {
			path += "/";
		}
		path = normalizePath(path);
		if (!isHierarchicalDirectory()) {
			try {
				ObjectMetadata om = this.s3.getObjectMetadata(getBucket(), path);
				return om.getContentLength() == 0;
			}
			catch (AmazonS3Exception ex) {
				if (NOT_FOUND.equals(ex.getErrorCode())) {
					return false;
				}
				throw ex;
			}
		}
		else {
			if (!listFilesAndDirectory(path, 1, null).getResult().isEmpty()) {
				return true;
			}
			int index = StringUtils.trimTrailingCharacter(path, '/').lastIndexOf('/');
			String parent = index > 0 ? path.substring(0, index) : "/";
			String marker = null;
			do {
				Paged<FileInfo> paged = listFilesAndDirectory(parent, getBatchSize(), marker);
				List<FileInfo> result = paged.getResult();
				if ((result == null) || result.isEmpty()) {
					return false;
				}
				for (FileInfo f : result) {
					if (f.getName().equals(path.substring(index + 1, path.length() - 1)) && !f.isFile()) {
						return true;
					}
				}
				marker = paged.getNextMarker();
			}
			while (marker != null);
			return false;
		}
	}

	@Override
	public boolean rename(String fromPath, String toPath) {
		fromPath = normalizePath(fromPath);
		toPath = normalizePath(toPath);
		if (!exists(fromPath)) {
			return false;
		}
		if (!this.isHierarchicalDirectory()) {
			int index = fromPath.lastIndexOf('/');
			String parentFrom = index > 0 ? fromPath.substring(0, index + 1) : "/";
			index = toPath.lastIndexOf('/');
			String parentTo = index > 0 ? toPath.substring(0, index + 1) : "/";
			if (!parentFrom.startsWith(parentTo)) {
				mkdir(parentTo);
			}
		}
		this.s3.copyObject(getBucket(), fromPath, getBucket(), toPath);
		this.s3.deleteObject(getBucket(), fromPath);
		return true;
	}

	@Override
	protected Paged<FileInfo> doListFilesAndDirectory(String path, int limit, String marker) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		if ((marker == null) && !isHierarchicalDirectory()) {
			limit += 1;
		}
		path = normalizePath(path);
		List<FileInfo> list = new ArrayList<>();
		ListObjectsRequest request = new ListObjectsRequest(getBucket(), path, marker, "/", limit);
		ObjectListing objectListing = this.s3.listObjects(request);
		for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
			String name = objectSummary.getKey();
			name = name.substring(path.length());
			if (!name.isEmpty()) {
				list.add(new FileInfo(name, true, objectSummary.getSize(), objectSummary.getLastModified().getTime()));
			}
		}
		for (String s : objectListing.getCommonPrefixes()) {
			s = s.substring(path.length(), s.length() - 1);
			list.add(new FileInfo(s, false));
		}
		return new Paged<>(marker, objectListing.getNextMarker(), list);
	}

}
