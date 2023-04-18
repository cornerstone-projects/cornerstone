plugins {
	java
	checkstyle
	id("io.freefair.lombok")
	id("io.spring.dependency-management")
	id("io.spring.javaformat")
}

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	val repoUrlPrefix: String? by rootProject
	if (repoUrlPrefix != null) {
		maven {
			url = uri("${repoUrlPrefix}/maven-public/")
			isAllowInsecureProtocol = true
		}
	} else {
		mavenCentral()
	}
}

dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
		mavenBom("""org.testcontainers:testcontainers-bom:${property("testcontainers.version")}""")
	}
	generatedPomCustomization {
		enabled(false)
	}
}

dependencies {
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("com.h2database:h2")
	testImplementation(testFixtures(project(":cornerstone-core")))
	checkstyle("""io.spring.javaformat:spring-javaformat-checkstyle:${property("javaformat-plugin.version")}""")
}

configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.name == "spring-boot-starter-logging")
			useTarget("${requested.group}:spring-boot-starter-log4j2:${requested.version}")
	}
}

testing {
	suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter()
		}
		register<JvmTestSuite>("integrationTest") {
			sources {
				compileClasspath += sourceSets.test.get().output
				runtimeClasspath += sourceSets.test.get().output
			}
			dependencies {
				implementation(project())
				configurations.testImplementation {
					dependencies.forEach { implementation(it) }
				}
				configurations.testRuntimeOnly {
					dependencies.forEach { runtimeOnly(it) }
				}
			}
			targets {
				all {
					testTask.configure {
						shouldRunAfter(test)
					}
				}
			}
		}
	}
}

java {
	withSourcesJar()
}

tasks.jar {
	manifest {
		attributes(
			mapOf(
				"Implementation-Title" to name,
				"Implementation-Version" to version,
				"Automatic-Module-Name" to name.replace("-", ".")  // for Jigsaw
			)
		)
	}
}

tasks.named<Jar>("sourcesJar") {
	val delombok by tasks.existing
	dependsOn(delombok)
	from("build/generated/sources/delombok/java/main/")
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile>() {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-parameters")
}

tasks.clean {
	doLast {
		delete("bin", "logs")
	}
}

val integration: String? by rootProject
if (integration != null) {
	val check by tasks.existing
	val integrationTest by tasks.existing
	check.get().dependsOn(integrationTest)
}
