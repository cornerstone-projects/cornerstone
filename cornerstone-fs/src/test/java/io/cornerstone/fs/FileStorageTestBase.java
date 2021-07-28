package io.cornerstone.fs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.cornerstone.SpringApplicationTest;

@SpringApplicationTest(classes = FileStorageAutoConfiguration.class, webEnvironment = WebEnvironment.NONE)
public abstract class FileStorageTestBase {

	@Autowired
	protected FileStorage fs;

	@BeforeEach
	public void cleanup() {
		delete(fs, "/");
	}

	@Test
	public void testDirectory() throws IOException {
		assertThat(fs.isDirectory("/test")).isFalse();
		assertThat(fs.mkdir("/test")).isTrue();
		assertThat(fs.isDirectory("/test")).isTrue();
		assertThat(fs.delete("/test")).isTrue();
		assertThat(fs.isDirectory("/test/test2")).isFalse();
		assertThat(fs.mkdir("/test/test2")).isTrue();
		assertThat(fs.isDirectory("/test/test2")).isTrue();
		assertThat(fs.delete("/test/test2")).isTrue();
		assertThat(fs.delete("/test")).isTrue();
	}

	@Test
	public void testFile() throws IOException {
		String text = "test";
		String path = "/test/test2/test.txt";
		String path2 = "/test/test2/test2.txt";
		writeToFile(fs, text, path);
		writeToFile(fs, text, path2);
		assertThat(fs.isDirectory("/test")).isTrue();
		assertThat(fs.isDirectory("/test/test2/")).isTrue();
		assertThat(fs.open("/test/test2/")).isNull();
		assertThat(fs.open("/test/test2/notexists.txt")).isNull();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(path), StandardCharsets.UTF_8))) {
			assertThat(br.lines().collect(Collectors.joining("\n"))).isEqualTo(text);
		}
		assertThat(fs.exists("/test/")).isTrue();
		assertThat(fs.exists(path)).isTrue();
		assertThat(fs.delete("/test/test2/")).isFalse();
		fs.delete(path);
		assertThat(fs.delete("/test/test2/")).isFalse();
		fs.delete(path2);
		fs.delete("/test/test2/");
		fs.delete("/test/");
	}

	@Test
	public void testRenameFile() throws IOException {
		String text = "test";
		String path = "/test/test2/test.txt";
		String path2 = "/test/test2/test2.txt";
		String path3 = "/test/test3/test3.txt";
		writeToFile(fs, text, path);
		assertThat(fs.rename(path, path2)).isTrue();
		assertThat(fs.exists(path)).isFalse();
		assertThat(fs.exists(path2)).isTrue();
		assertThat(fs.isDirectory(path2.substring(0, path2.lastIndexOf('/')))).isTrue();
		assertThat(fs.rename(path2, path3)).isTrue();
		assertThat(fs.exists(path2)).isFalse();
		assertThat(fs.exists(path3)).isTrue();
		assertThat(fs.isDirectory(path3.substring(0, path3.lastIndexOf('/')))).isTrue();
		fs.delete(path3);
		fs.delete(path3.substring(0, path3.lastIndexOf('/')));
		fs.delete(path2.substring(0, path2.lastIndexOf('/')));
		fs.delete("/test/");
	}

	@Test
	public void testListFiles() throws IOException {
		fs.mkdir("/test");
		List<FileInfo> files = fs.listFiles("/");
		assertThat(files).isEmpty();
		List<FileInfo> fileList = fs.listFilesAndDirectory("/");
		assertThat(fileList).hasSize(1);
		assertThat(isFile(fileList, "test")).isFalse();
		writeToFile(fs, "test", "/test.txt");
		files = fs.listFiles("/");
		assertThat(files).hasSize(1);
		fileList = fs.listFilesAndDirectory("/");
		assertThat(fileList).hasSize(2);
		assertThat(isFile(fileList, "test.txt")).isTrue();
		assertThat(isFile(fileList, "test")).isFalse();

		fs.mkdir("/test/test2");
		files = fs.listFiles("/test");
		assertThat(files.isEmpty()).isTrue();
		fileList = fs.listFilesAndDirectory("/test");
		assertThat(fileList).hasSize(1);
		assertThat(isFile(fileList, "test2")).isFalse();
		writeToFile(fs, "test", "/test/test.txt");
		files = fs.listFiles("/test");
		assertThat(files).hasSize(1);
		fileList = fs.listFilesAndDirectory("/test");
		assertThat(fileList).hasSize(2);
		assertThat(isFile(fileList, "test.txt")).isTrue();
		assertThat(isFile(fileList, "test2")).isFalse();

		fs.delete("/test/test2/");
		fs.delete("/test/test.txt");
		fs.delete("/test.txt");
		fs.delete("/test");
	}

	@Test
	public void testListFilesWithMarker() throws IOException {
		String dir = "/test";

		// prepare
		String text = "test";
		for (int i = 0; i < 5; i++)
			fs.mkdir(dir + "/testdir" + i);
		for (int i = 0; i < 5; i++)
			writeToFile(fs, text, dir + "/test" + i + ".txt");

		int total = 0;
		int limit = 2;
		Paged<FileInfo> paged;
		String marker = null;
		do {
			paged = fs.listFiles(dir, limit, marker);
			marker = paged.getNextMarker();
			int size = paged.getResult().size();
			total += size;
			assertThat(size).isLessThanOrEqualTo(limit);
		} while (marker != null);
		assertThat(total).isEqualTo(5);

		total = 0;
		limit = 5;
		do {
			paged = fs.listFilesAndDirectory(dir, limit, marker);
			marker = paged.getNextMarker();
			int size = paged.getResult().size();
			total += size;
			assertThat(size).isLessThanOrEqualTo(limit);
		} while (marker != null);
		assertThat(total).isEqualTo(10);

		// cleanup
		for (int i = 0; i < 5; i++)
			fs.delete(dir + "/test" + i + ".txt");
		for (int i = 0; i < 5; i++)
			fs.delete(dir + "/testdir" + i);
		fs.delete(dir);
	}

	private static boolean isFile(List<FileInfo> files, String name) {
		for (FileInfo file : files) {
			if (file.getName().equals(name))
				return file.isFile();
		}
		throw new IllegalArgumentException("file '" + name + "' not found");
	}

	protected static void writeToFile(FileStorage fs, String text, String path) throws IOException {
		byte[] bytes = text.getBytes();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		fs.write(is, path, bytes.length);
	}

	protected static void delete(FileStorage fs, String directory) {
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
				delete(fs, path);
			}
		}
		if (!directory.equals("/"))
			fs.delete(directory);
	}

}
