package io.cornerstone.fs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import io.cornerstone.test.SpringApplicationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringApplicationTest(classes = FileStorageAutoConfiguration.class, webEnvironment = WebEnvironment.NONE)
abstract class FileStorageTestBase {

	@Autowired
	protected FileStorage fs;

	@BeforeEach
	void cleanup() {
		delete(this.fs, "/");
	}

	@Test
	void testDirectory() {
		assertThat(this.fs.isDirectory("/test")).isFalse();
		assertThat(this.fs.mkdir("/test")).isTrue();
		assertThat(this.fs.isDirectory("/test")).isTrue();
		assertThat(this.fs.delete("/test")).isTrue();
		assertThat(this.fs.isDirectory("/test/test2")).isFalse();
		assertThat(this.fs.mkdir("/test/test2")).isTrue();
		assertThat(this.fs.isDirectory("/test/test2")).isTrue();
		assertThat(this.fs.delete("/test/test2")).isTrue();
		assertThat(this.fs.delete("/test")).isTrue();
	}

	@Test
	void testFile() throws IOException {
		String text = "test";
		String path = "/test/test2/test.txt";
		String path2 = "/test/test2/test2.txt";
		writeToFile(this.fs, text, path);
		writeToFile(this.fs, text, path2);
		assertThat(this.fs.isDirectory("/test")).isTrue();
		assertThat(this.fs.isDirectory("/test/test2/")).isTrue();
		assertThat(this.fs.open("/test/test2/")).isNull();
		assertThat(this.fs.open("/test/test2/notexists.txt")).isNull();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(this.fs.open(path), StandardCharsets.UTF_8))) {
			assertThat(br.lines().collect(Collectors.joining("\n"))).isEqualTo(text);
		}
		assertThat(this.fs.exists("/test/")).isTrue();
		assertThat(this.fs.exists(path)).isTrue();
		assertThat(this.fs.delete("/test/test2/")).isFalse();
		this.fs.delete(path);
		assertThat(this.fs.delete("/test/test2/")).isFalse();
		this.fs.delete(path2);
		this.fs.delete("/test/test2/");
		this.fs.delete("/test/");
	}

	@Test
	void testRenameFile() throws IOException {
		String text = "test";
		String path = "/test/test2/test.txt";
		String path2 = "/test/test2/test2.txt";
		String path3 = "/test/test3/test3.txt";
		writeToFile(this.fs, text, path);
		assertThat(this.fs.rename(path, path2)).isTrue();
		assertThat(this.fs.exists(path)).isFalse();
		assertThat(this.fs.exists(path2)).isTrue();
		assertThat(this.fs.isDirectory(path2.substring(0, path2.lastIndexOf('/')))).isTrue();
		assertThat(this.fs.rename(path2, path3)).isTrue();
		assertThat(this.fs.exists(path2)).isFalse();
		assertThat(this.fs.exists(path3)).isTrue();
		assertThat(this.fs.isDirectory(path3.substring(0, path3.lastIndexOf('/')))).isTrue();
		this.fs.delete(path3);
		this.fs.delete(path3.substring(0, path3.lastIndexOf('/')));
		this.fs.delete(path2.substring(0, path2.lastIndexOf('/')));
		this.fs.delete("/test/");
	}

	@Test
	void testListFiles() throws IOException {
		this.fs.mkdir("/test");
		List<FileInfo> files = this.fs.listFiles("/");
		assertThat(files).isEmpty();
		List<FileInfo> fileList = this.fs.listFilesAndDirectory("/");
		assertThat(fileList).hasSize(1);
		assertThat(isFile(fileList, "test")).isFalse();
		writeToFile(this.fs, "test", "/test.txt");
		files = this.fs.listFiles("/");
		assertThat(files).hasSize(1);
		fileList = this.fs.listFilesAndDirectory("/");
		assertThat(fileList).hasSize(2);
		assertThat(isFile(fileList, "test.txt")).isTrue();
		assertThat(isFile(fileList, "test")).isFalse();

		this.fs.mkdir("/test/test2");
		files = this.fs.listFiles("/test");
		assertThat(files.isEmpty()).isTrue();
		fileList = this.fs.listFilesAndDirectory("/test");
		assertThat(fileList).hasSize(1);
		assertThat(isFile(fileList, "test2")).isFalse();
		writeToFile(this.fs, "test", "/test/test.txt");
		files = this.fs.listFiles("/test");
		assertThat(files).hasSize(1);
		fileList = this.fs.listFilesAndDirectory("/test");
		assertThat(fileList).hasSize(2);
		assertThat(isFile(fileList, "test.txt")).isTrue();
		assertThat(isFile(fileList, "test2")).isFalse();

		this.fs.delete("/test/test2/");
		this.fs.delete("/test/test.txt");
		this.fs.delete("/test.txt");
		this.fs.delete("/test");
	}

	@Test
	void testListFilesWithMarker() throws IOException {
		String dir = "/test";

		// prepare
		String text = "test";
		for (int i = 0; i < 5; i++) {
			this.fs.mkdir(dir + "/testdir" + i);
		}
		for (int i = 0; i < 5; i++) {
			writeToFile(this.fs, text, dir + "/test" + i + ".txt");
		}

		int total = 0;
		int limit = 2;
		Paged<FileInfo> paged;
		String marker = null;
		do {
			paged = this.fs.listFiles(dir, limit, marker);
			marker = paged.getNextMarker();
			int size = paged.getResult().size();
			total += size;
			assertThat(size).isLessThanOrEqualTo(limit);
		}
		while (marker != null);
		assertThat(total).isEqualTo(5);

		total = 0;
		limit = 5;
		do {
			paged = this.fs.listFilesAndDirectory(dir, limit, marker);
			marker = paged.getNextMarker();
			int size = paged.getResult().size();
			total += size;
			assertThat(size).isLessThanOrEqualTo(limit);
		}
		while (marker != null);
		assertThat(total).isEqualTo(10);

		// cleanup
		for (int i = 0; i < 5; i++) {
			this.fs.delete(dir + "/test" + i + ".txt");
		}
		for (int i = 0; i < 5; i++) {
			this.fs.delete(dir + "/testdir" + i);
		}
		this.fs.delete(dir);
	}

	private static boolean isFile(List<FileInfo> files, String name) {
		for (FileInfo file : files) {
			if (file.getName().equals(name)) {
				return file.isFile();
			}
		}
		throw new IllegalArgumentException("file '" + name + "' not found");
	}

	protected static void writeToFile(FileStorage fs, String text, String path) throws IOException {
		byte[] bytes = text.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		fs.write(is, path, bytes.length);
	}

	protected static void delete(FileStorage fs, String directory) {
		if (directory == null) {
			directory = "/";
		}
		if (!directory.endsWith("/")) {
			directory = directory + "/";
		}
		List<FileInfo> files = fs.listFilesAndDirectory(directory);
		for (FileInfo entry : files) {
			String path = directory + entry.getName();
			if (entry.isFile()) {
				fs.delete(path);
			}
			else {
				delete(fs, path);
			}
		}
		if (!directory.equals("/")) {
			fs.delete(directory);
		}
	}

}
