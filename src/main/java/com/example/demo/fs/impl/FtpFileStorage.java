package com.example.demo.fs.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.StringUtils;

import com.example.demo.core.util.FileUtils;
import com.example.demo.fs.FileInfo;
import com.example.demo.fs.FileStorageProperties;
import com.example.demo.fs.FileStorageProperties.Ftp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FtpFileStorage extends AbstractFileStorage {

	private final URI uri;
	private final Ftp config;

	private ObjectPool<FTPClient> pool;

	public FtpFileStorage(FileStorageProperties fileStorageProperties) {
		this.uri = fileStorageProperties.getUri();
		this.config = fileStorageProperties.getFtp();
	}

	@PostConstruct
	public void init() {
		PooledObjectFactory<FTPClient> factory = new BasePooledObjectFactory<FTPClient>() {

			@Override
			public FTPClient create() throws Exception {
				FTPClient ftpClient = uri.getScheme().equals("ftps") ? new FTPSClient() : new FTPClient();
				ftpClient.setDefaultTimeout(config.getDefaultTimeout());
				ftpClient.setDataTimeout(config.getDataTimeout());
				ftpClient.setControlEncoding(config.getControlEncoding());
				ftpClient.connect(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : ftpClient.getDefaultPort());
				if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					ftpClient.disconnect();
					throw new IOException("FTP server refused connection.");
				}
				String userInfo = uri.getUserInfo();
				boolean b;
				if (userInfo != null) {
					String[] arr = userInfo.split(":", 2);
					b = ftpClient.login(arr[0], arr.length > 1 ? arr[1] : null);
				} else {
					b = ftpClient.login("anonymous", "anonymous@test.com");
				}
				if (!b) {
					ftpClient.logout();
					throw new IllegalArgumentException("Invalid username or password");
				}
				ftpClient.setFileType(config.isBinaryMode() ? FTP.BINARY_FILE_TYPE : FTP.ASCII_FILE_TYPE);
				if (config.isPassiveMode()) {
					ftpClient.enterLocalPassiveMode();
				} else {
					ftpClient.enterLocalActiveMode();
				}
				return ftpClient;
			}

			@Override
			public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
				return new DefaultPooledObject<>(ftpClient);
			}

			@Override
			public boolean validateObject(PooledObject<FTPClient> po) {
				FTPClient ftpClient = po.getObject();
				try {
					return ftpClient.sendNoOp() && ftpClient.printWorkingDirectory() != null;
				} catch (IOException e) {
					return false;
				}
			}

			@Override
			public void destroyObject(PooledObject<FTPClient> po) {
				FTPClient ftpClient = po.getObject();
				if (ftpClient.isConnected()) {
					try {
						ftpClient.logout();
					} catch (FTPConnectionClosedException e) {
						// Ignore
					} catch (IOException e) {
						if (!e.getMessage().contains("Broken pipe"))
							log.error(e.getMessage(), e);
					} finally {
						try {
							ftpClient.disconnect();
						} catch (FTPConnectionClosedException e) {
							// Ignore
						} catch (IOException e) {
							if (!e.getMessage().contains("Broken pipe"))
								log.error(e.getMessage(), e);
						}
					}
				}
			}
		};
		GenericObjectPoolConfig<FTPClient> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMaxTotal(config.getPool().getMaxTotal());
		poolConfig.setMaxIdle(config.getPool().getMaxIdle());
		poolConfig.setMinIdle(config.getPool().getMinIdle());
		poolConfig.setMaxWaitMillis(config.getPool().getMaxWaitMillis());
		poolConfig.setMinEvictableIdleTimeMillis(config.getPool().getMinEvictableIdleTimeMillis());
		poolConfig.setSoftMinEvictableIdleTimeMillis(poolConfig.getMinEvictableIdleTimeMillis());
		poolConfig.setLifo(false);
		poolConfig.setTestOnBorrow(true);
		pool = new GenericObjectPool<FTPClient>(factory, poolConfig);
	}

	@PreDestroy
	public void destroy() {
		pool.close();
	}

	@Override
	public void write(InputStream is, String path) throws IOException {
		try (InputStream ins = is) {
			execute(ftpClient -> {
				String pathname = getPathname(path, ftpClient);
				String workingDirectory = ftpClient.printWorkingDirectory();
				workingDirectory = trimTailSlash(workingDirectory);
				String relativePath = pathname.substring(workingDirectory.length() + 1);
				String[] arr = relativePath.split("/");
				if (arr.length > 1) {
					StringBuilder sb = new StringBuilder(workingDirectory);
					for (int i = 0; i < arr.length - 1; i++) {
						sb.append("/").append(arr[i]);
						ftpClient.changeWorkingDirectory(sb.toString());
						if (ftpClient.getReplyCode() == 550) {
							ftpClient.makeDirectory(sb.toString());
						}
					}
				}
				ftpClient.storeFile(pathname, ins);
				return null;
			});
		}
	}

	@Override
	public InputStream open(String path) throws IOException {
		return execute(ftpClient -> {
			String pathname = getPathname(path, ftpClient);
			FTPFile[] files = ftpClient.listFiles(pathname);
			if (files == null || files.length != 1 || !files[0].isFile())
				return null;
			long size = files[0].getSize();
			if (size <= config.getBufferThreshold()) {
				// small file
				ByteArrayOutputStream bos = new ByteArrayOutputStream((int) size);
				ftpClient.retrieveFile(pathname, bos);
				return new ByteArrayInputStream(bos.toByteArray());
			}
			return new FilterInputStream(ftpClient.retrieveFileStream(pathname)) {

				private boolean closed;

				@Override
				public void close() throws IOException {
					if (closed)
						return;
					closed = true;
					try {
						super.close();
					} finally {
						try {
							ftpClient.completePendingCommand();
							pool.returnObject(ftpClient);
						} catch (Exception e) {
							try {
								pool.invalidateObject(ftpClient);
							} catch (Exception e1) {
								log.error(e1.getMessage(), e1);
							}
							log.error(e.getMessage(), e);
						}
					}
				}
			};
		});
	}

	@Override
	public boolean mkdir(String path) {
		return executeWrapped(ftpClient -> {
			String pathname = getPathname(path, ftpClient);
			String workingDirectory = ftpClient.printWorkingDirectory();
			workingDirectory = trimTailSlash(workingDirectory);
			String relativePath = pathname.substring(workingDirectory.length() + 1);
			String[] arr = relativePath.split("/");
			StringBuilder sb = new StringBuilder(workingDirectory);
			for (int i = 0; i < arr.length; i++) {
				sb.append("/").append(arr[i]);
				ftpClient.changeWorkingDirectory(sb.toString());
				if (ftpClient.getReplyCode() == 550) {
					if (!ftpClient.makeDirectory(sb.toString()))
						return false;
				}
			}
			return true;
		});
	}

	@Override
	public boolean delete(String path) {
		return executeWrapped(ftpClient -> {
			String pathname = getPathname(path, ftpClient);
			ftpClient.changeWorkingDirectory(pathname);
			if (ftpClient.getReplyCode() != 550) {
				ftpClient.changeToParentDirectory();
				return ftpClient.removeDirectory(pathname);
			} else {
				return ftpClient.deleteFile(pathname);
			}
		});
	}

	@Override
	public long getLastModified(String path) {
		return executeWrapped(ftpClient -> {
			String modificationTime = ftpClient.getModificationTime(getPathname(path, ftpClient));
			if (modificationTime != null)
				try {
					Date d = new SimpleDateFormat("yyyyMMddHHmmss").parse(modificationTime);
					return d.getTime() + (config.isUseLocaltime() ? 0 : TimeZone.getDefault().getRawOffset());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			return 0L;
		});

	}

	@Override
	public boolean exists(String path) {
		boolean isFile = executeWrapped(ftpClient -> {
			return ftpClient.getModificationTime(getPathname(path, ftpClient)) != null;
		});
		return isFile || isDirectory(path);
	}

	@Override
	public boolean rename(String fromPath, String toPath) {
		String fromPath_ = FileUtils.normalizePath(fromPath);
		String toPath_ = FileUtils.normalizePath(toPath);
		int index = fromPath_.lastIndexOf('/');
		String parentFrom = index > 0 ? fromPath_.substring(0, index + 1) : "/";
		index = toPath_.lastIndexOf('/');
		String parentTo = index > 0 ? toPath_.substring(0, index + 1) : "/";
		if (!parentFrom.startsWith(parentTo))
			mkdir(parentTo);
		return executeWrapped(ftpClient -> {
			return ftpClient.rename(getPathname(fromPath_, ftpClient), getPathname(toPath_, ftpClient));
		});
	}

	@Override
	public boolean isDirectory(String path) {
		if (path.isEmpty() || path.equals("/"))
			return true;
		return executeWrapped(ftpClient -> {
			ftpClient.changeWorkingDirectory(getPathname(path, ftpClient));
			return ftpClient.getReplyCode() != 550;
		});
	}

	@Override
	public List<FileInfo> listFiles(String path) {
		List<FileInfo> result = executeWrapped(ftpClient -> {
			List<FileInfo> list = new ArrayList<>();
			for (FTPFile f : ftpClient.listFiles(getPathname(path, ftpClient))) {
				if (f.isFile())
					list.add(new FileInfo(f.getName(), true, f.getSize(), f.getTimestamp().getTimeInMillis()
							+ (config.isUseLocaltime() ? 0 : TimeZone.getDefault().getRawOffset())));
				if (list.size() > MAX_PAGE_SIZE)
					throw new IllegalArgumentException("Exceed max size:" + MAX_PAGE_SIZE);
			}
			return list;
		});
		result.sort(COMPARATOR);
		return result;
	}

	@Override
	public List<FileInfo> listFilesAndDirectory(String path) {
		List<FileInfo> result = executeWrapped(ftpClient -> {
			final List<FileInfo> list = new ArrayList<>();
			for (FTPFile f : ftpClient.listFiles(getPathname(path, ftpClient)))
				list.add(new FileInfo(f.getName(), f.isFile(), f.getSize(), f.getTimestamp().getTimeInMillis()
						+ (config.isUseLocaltime() ? 0 : TimeZone.getDefault().getRawOffset())));
			if (list.size() > MAX_PAGE_SIZE)
				throw new IllegalArgumentException("Exceed max size:" + MAX_PAGE_SIZE);
			return list;
		});
		result.sort(COMPARATOR);
		return result;
	}

	private String getPathname(String path, FTPClient ftpClient) throws IOException {
		if (!path.startsWith("/"))
			path = "/" + path;
		String wd = !StringUtils.hasLength(config.getWorkingDirectory()) ? ftpClient.printWorkingDirectory()
				: config.getWorkingDirectory();
		return FileUtils.normalizePath(wd + uri.getPath() + path);
	}

	protected <T> T executeWrapped(Callback<T> callback) {
		try {
			return execute(callback);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public <T> T execute(Callback<T> callback) throws IOException {
		FTPClient ftpClient = null;
		boolean deferReturn = false;
		try {
			ftpClient = pool.borrowObject();
			String workingDirectory = ftpClient.printWorkingDirectory();
			T val = callback.doWithFTPClient(ftpClient);
			if (!(val instanceof FilterInputStream)) {
				ftpClient.changeWorkingDirectory(workingDirectory);
			} else {
				deferReturn = true;
			}
			return val;
		} catch (IOException e) {
			if (ftpClient != null)
				try {
					pool.invalidateObject(ftpClient);
					ftpClient = null;
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (ftpClient != null && !deferReturn)
				try {
					pool.returnObject(ftpClient);
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
		}
	}

	interface Callback<T> {
		T doWithFTPClient(FTPClient ftpClient) throws IOException;
	}

}
