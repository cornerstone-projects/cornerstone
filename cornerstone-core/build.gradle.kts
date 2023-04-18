plugins {
	id("io.cornerstone.java-library-conventions")
	`java-test-fixtures`
}

dependencies {
	api("org.springframework.boot:spring-boot-starter-log4j2")
	api("org.springframework.boot:spring-boot-starter-web")
	api("org.springframework.boot:spring-boot-starter-cache")
	api("org.springframework.boot:spring-boot-starter-data-redis")
	api("org.springframework.boot:spring-boot-starter-data-jpa")
	api("org.springframework.boot:spring-boot-starter-validation")
	api("org.springframework.boot:spring-boot-starter-security")
	api("org.springframework.boot:spring-boot-starter-actuator")
	api("org.springframework.kafka:spring-kafka")
	api("org.springframework.session:spring-session-data-redis")
	api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	api("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
	api("com.fasterxml.jackson.dataformat:jackson-dataformat-smile")
	api("com.fasterxml.jackson.module:jackson-module-mrbean")
	api("com.fasterxml.jackson.module:jackson-module-parameter-names")
	api("""io.github.resilience4j:resilience4j-spring-boot2:${property("resilience4j.version")}""")
	api("""io.opentracing.contrib:opentracing-spring-jaeger-web-starter:${property("java-spring-jaeger.version")}""")
	api("""io.opentracing.contrib:opentracing-jdbc:${property("opentracing-jdbc.version")}""")
	api("""org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdoc.version")}""")
	api("org.apache.commons:commons-pool2")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("io.prometheus:simpleclient_pushgateway")
	compileOnly("org.springframework.boot:spring-boot-starter-amqp")
	testFixturesApi("org.springframework.boot:spring-boot-starter-test")
	testFixturesApi("org.springframework.security:spring-security-test")
	testFixturesApi("org.testcontainers:testcontainers")
	testFixturesApi("org.testcontainers:junit-jupiter")
	testFixturesImplementation("org.testcontainers:mysql")
	testFixturesImplementation("org.testcontainers:postgresql")
	testFixturesImplementation("org.testcontainers:mssqlserver")
	testFixturesImplementation("org.testcontainers:oracle-xe")
	testFixturesImplementation("org.testcontainers:db2")
	testFixturesImplementation("org.testcontainers:rabbitmq")
	integrationTestImplementation("org.springframework.boot:spring-boot-starter-amqp")
	integrationTestRuntimeOnly("com.mysql:mysql-connector-j")
	integrationTestRuntimeOnly("org.postgresql:postgresql")
	integrationTestRuntimeOnly("com.microsoft.sqlserver:mssql-jdbc")
	integrationTestRuntimeOnly("com.ibm.db2:jcc")
	integrationTestRuntimeOnly("com.oracle.database.jdbc:ojdbc8")
}
