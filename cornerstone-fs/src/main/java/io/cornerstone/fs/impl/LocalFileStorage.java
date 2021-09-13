package io.cornerstone.fs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import io.cornerstone.core.util.FileUtils;
import io.cornerstone.fs.FileInfo;
import io.cornerstone.fs.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalFileStorage extends AbstractFileStorage {

	private final URI uri;

	private File directory;

	public LocalFileStorage(FileStorageProperties fileStorageProperties) {
		this.uri = fileStorageProperties.getUri();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		Assert.notNull(this.uri, "uri shouldn't be null");
		this.directory = this.uri.isAbsolute() ? new File(this.uri) : new File(this.uri.getPath());
		if (this.directory.isFile())
			throw new IllegalStateException(this.directory + " is not directory");
		if (!this.directory.exists())
			if (!this.directory.mkdirs())
				log.error("mkdirs error:" + this.directory.getAbsolutePath());
	}

	@Override
	public void write(InputStream is, String path) throws IOException {
		path = normalizePath(path);
		File dest = new File(this.directory, path);
		dest.getParentFile().mkdirs();
		try (InputStream ins = is; FileOutputStream os = new FileOutputStream(dest)) {
			StreamUtils.copy(ins, os);
		}
	}

	@Override
	public InputStream open(String path) throws IOException {
		path = normalizePath(path);
		File file = new File(this.directory, path);
		if (!file.exists() || file.isDirectory())
			return null;
		return new FileInputStream(file);
	}

	@Override
	public boolean mkdir(String path) {
		path = normalizePath(path);
		return new File(this.directory, path).mkdirs();
	}

	@Override
	public boolean delete(String path) {
		path = normalizePath(path);
		return new File(this.directory, path).delete();
	}

	@Override
	public long getLastModified(String path) {
		path = normalizePath(path);
		return new File(this.directory, path).lastModified();
	}

	@Override
	public boolean exists(String path) {
		path = normalizePath(path);
		return new File(this.directory, path).exists();
	}

	@Override
	public boolean rename(String fromPath, String toPath) {
		fromPath = normalizePath(this.directory.getPath() + "/" + fromPath);
		toPath = normalizePath(this.directory.getPath() + "/" + toPath);
		File source = new File(fromPath);
		File target = new File(toPath);
		if (source.getParent().equals(target.getParent())) {
			return source.renameTo(target);
		} else {
			return target.getParentFile().mkdirs() && source.renameTo(target);
		}
	}

	@Override
	public boolean isDirectory(String path) {
		if (path.isEmpty() || path.equals("/"))
			return true;
		path = normalizePath(path);
		return new File(this.directory, path).isDirectory();
	}

	@Override
	public List<FileInfo> listFiles(String path) {
		path = normalizePath(path);
		final List<FileInfo> list = new ArrayList<>();
		new File(this.directory, path).listFiles(f -> {
			if (f.isFile()) {
				list.add(new FileInfo(f.getName(), true, f.length(), f.lastModified()));
				if (list.size() > MAX_PAGE_SIZE)
					throw new IllegalArgumentException("Exceed max size:" + MAX_PAGE_SIZE);
			}
			return false;
		});
		list.sort(COMPARATOR);
		return list;
	}

	@Override
	public List<FileInfo> listFilesAndDirectory(String path) {
		path = normalizePath(path);
		final List<FileInfo> list = new ArrayList<>();
		new File(this.directory, path).listFiles(f -> {
			list.add(new FileInfo(f.getName(), f.isFile(), f.length(), f.lastModified()));
			if (list.size() > MAX_PAGE_SIZE)
				throw new IllegalArgumentException("Exceed max size:" + MAX_PAGE_SIZE);
			return false;
		});
		list.sort(COMPARATOR);
		return list;
	}

	private String normalizePath(String path) {
		if (!path.startsWith("/"))
			path = "/" + path;
		return FileUtils.normalizePath(path);
	}

}
