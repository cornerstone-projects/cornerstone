package io.cornerstone.fs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.cornerstone.fs.impl.LocalFileStorage;

class FileStorageMigrationTests {

	static FileStorage source;

	static FileStorage target;

	@BeforeAll
	static void setup() {
		FileStorageProperties props1 = new FileStorageProperties();
		props1.setUri(URI.create("file:///tmp/fs1"));
		LocalFileStorage fs1 = new LocalFileStorage(props1);
		fs1.afterPropertiesSet();
		source = fs1;
		FileStorageProperties props2 = new FileStorageProperties();
		props2.setUri(URI.create("file:///tmp/fs2"));
		LocalFileStorage fs2 = new LocalFileStorage(props2);
		fs2.afterPropertiesSet();
		target = fs2;
	}

	@AfterAll
	static void cleanup() {
		try {
			cleanup(source, "/");
			cleanup(target, "/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testMigration() throws IOException {
		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				writeToFile(source, "text", "/test" + i + "/test" + j + ".txt");
		verify(source);
		source.migrateTo(target, "/", false);
		verify(source);
		verify(target);
		cleanup(target, "/");
		assertThat(target.listFilesAndDirectory("/")).isEmpty();
		source.migrateTo(target, "/", true);
		assertThat(source.listFilesAndDirectory("/")).isEmpty();
		verify(target);
	}

	protected static void verify(FileStorage fs) throws IOException {
		List<FileInfo> list = fs.listFilesAndDirectory("/");
		assertThat(list).hasSize(10);
		for (FileInfo file : list) {
			assertThat(file.isFile()).isFalse();
			List<FileInfo> list2 = fs.listFilesAndDirectory("/" + file.getName());
			assertThat(list).hasSize(10);
			for (FileInfo file2 : list2) {
				assertThat(file2.isFile()).isTrue();
			}
		}
	}

	protected static void cleanup(FileStorage fs, String directory) throws IOException {
		if (directory == null)
			directory = "/";
		if (!directory.endsWith("/"))
			directory = directory + "/";
		List<FileInfo> files = fs.listFilesAndDirectory(directory);
		for (FileInfo entry : files) {
			String path = directory + entry.getName();
			if (entry.isFile()) {
				fs.delete(path);
			} else {
				cleanup(target, path);
			}
		}
		fs.delete(directory);
	}

	protected static void writeToFile(FileStorage fs, String text, String path) throws IOException {
		byte[] bytes = text.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		fs.write(is, path, bytes.length);
	}

}
