package io.cornerstone.fs;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.TestPropertySource;

@Disabled
@TestPropertySource(properties = { "file-storage.s3.endpoint=http://127.0.0.1:9000", "file-storage.s3.bucket=test",
		"file-storage.s3.access-key=minioadmin", "file-storage.s3.secret-key=minioadmin",
		"file-storage.s3.hierarchical-directory=true" })
public class MinioFileStorageTests extends FileStorageTestBase {

}
