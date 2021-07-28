package io.cornerstone.fs;

import org.junit.jupiter.api.Disabled;
import org.springframework.test.context.TestPropertySource;

@Disabled
@TestPropertySource(properties = { "file-storage.s3.endpoint=http://10.0.2.192:7480", "file-storage.s3.bucket=test",
		"file-storage.s3.access-key=ZUY5KM78QLGTLZCZ3LXU",
		"file-storage.s3.secret-key=ugHBPzL7KnmdgPDWlJDSj5G182f08oW1VxMmmTeP" })
public class CephFileStorageTests extends FileStorageTestBase {

}
