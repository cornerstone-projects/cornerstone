@file:Suppress("UnstableApiUsage")

import io.spring.javaformat.gradle.tasks.Format


plugins {
	java
	checkstyle
	id("io.freefair.lombok")
	id("io.spring.dependency-management")
	id("io.spring.javaformat")
	id("org.openrewrite.rewrite")
}

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
	val repoUrlPrefix: String? by rootProject
	if (repoUrlPrefix != null) {
		maven {
			url = uri("${repoUrlPrefix}/maven-public/")
			isAllowInsecureProtocol = true
		}
	} else {
		mavenCentral()
		maven {
			url = uri("https://repo.spring.io/milestone")
		}
	}
}

dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	}
	generatedPomCustomization {
		enabled(false)
	}
}

rewrite {
	activeRecipe("org.openrewrite.java.migrate.UpgradeToJava21")
}

dependencies {
	annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")
	testAnnotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("com.h2database:h2")
	testImplementation(testFixtures(project(":cornerstone-core")))
	checkstyle("""io.spring.javaformat:spring-javaformat-checkstyle:${property("javaformat-plugin.version")}""")
	rewrite("""org.openrewrite.recipe:rewrite-migrate-java:${property("rewrite-migrate-java.version")}""")
}

configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.name == "spring-boot-starter-logging")
			useTarget("${requested.group}:spring-boot-starter-log4j2:${requested.version}")
	}
	resolutionStrategy.dependencySubstitution {
		substitute(module("junit:junit"))
			.using(module("io.quarkus:quarkus-junit4-mock:3.5.0"))
			.because(
				"See https://github.com/testcontainers/testcontainers-java/issues/970"
			)
	}
}

testing {
	suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter()
		}
		val integrationTest by registering(JvmTestSuite::class) {
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

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-parameters")
}

tasks.named("clean") {
	doLast {
		delete("bin", "logs")
	}
}

tasks.withType<Format>() {
	dependsOn("rewriteRun")
}

val integration: String? by rootProject
if (integration != null) {
	val check by tasks.existing
	val integrationTest by tasks.existing
	check.get().dependsOn(integrationTest)
}
