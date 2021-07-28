package io.cornerstone.fs;

import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "file-storage.uri=file:///tmp/fs")
public class LocalFileStorageTests extends FileStorageTestBase {

}
