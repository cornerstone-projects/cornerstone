package io.cornerstone.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileUtilsTests {

	@Test
	void test() {
		assertThat(FileUtils.normalizePath("/")).isEqualTo("/");
		assertThat(FileUtils.normalizePath("test")).isEqualTo("test");
		assertThat(FileUtils.normalizePath("/test")).isEqualTo("/test");
		assertThat(FileUtils.normalizePath("/test/test")).isEqualTo("/test/test");
		assertThat(FileUtils.normalizePath("test/test")).isEqualTo("test/test");
		assertThat(FileUtils.normalizePath("/test//test")).isEqualTo("/test/test");
		assertThat(FileUtils.normalizePath("//test//test")).isEqualTo("/test/test");
		assertThat(FileUtils.normalizePath("./test")).isEqualTo("test");
		assertThat(FileUtils.normalizePath("//test/./test")).isEqualTo("/test/test");
		assertThat(FileUtils.normalizePath("./test/./test")).isEqualTo("test/test");
		assertThat(FileUtils.normalizePath("../test")).isEqualTo("/test");
		assertThat(FileUtils.normalizePath("//test/../test")).isEqualTo("/test");
		assertThat(FileUtils.normalizePath("../test/../test")).isEqualTo("/test");
	}

}
