@file:Suppress("UnstableApiUsage")

plugins {
	java
	checkstyle
	id("io.freefair.lombok")
	id("io.spring.dependency-management")
	id("io.spring.javaformat")
	id("com.societegenerale.commons.arch-unit-gradle-plugin")
}

java.sourceCompatibility = JavaVersion.VERSION_21

archUnit {
	if (project.name.endsWith("-bom")) {
		isSkip = true
	}
	preConfiguredRules = listOf(
		"com.societegenerale.commons.plugin.rules.NoInjectedFieldTest",
		"com.societegenerale.commons.plugin.rules.NoTestIgnoreWithoutCommentRuleTest",
		"com.societegenerale.commons.plugin.rules.NoPrefixForInterfacesRuleTest",
		"com.societegenerale.commons.plugin.rules.NoPowerMockRuleTest",
		"com.societegenerale.commons.plugin.rules.NoJodaTimeRuleTest",
		"com.societegenerale.commons.plugin.rules.NoJunitAssertRuleTest",
		"com.societegenerale.commons.plugin.rules.StringFieldsThatAreActuallyDatesRuleTest",
		"io.cornerstone.build.architecture.ArchitectureRuleTest"
	)
}

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

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")
	testAnnotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("com.h2database:h2")
	testImplementation(testFixtures(project(":cornerstone-core")))
	checkstyle("""io.spring.javaformat:spring-javaformat-checkstyle:${property("javaformat-plugin.version")}""")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
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
		val test by getting(JvmTestSuite::class)
		val integrationTest by registering(JvmTestSuite::class) {
			sources {
				compileClasspath += sourceSets.test.get().output
				runtimeClasspath += sourceSets.test.get().output
			}
			dependencies {
				implementation(project())
				configurations.implementation {
					dependencies.forEach { implementation(it) }
				}
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

		val integration: String? by rootProject
		if (integration != null) {
			val check by tasks.existing
			check.get().dependsOn(integrationTest)
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

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs(listOf("-javaagent:${mockitoAgent.asPath}", "-Xshare:off"))
}

tasks.named("clean") {
	doLast {
		delete("bin", "logs")
	}
}

tasks.register("checkstyle") {
	description = "Run Checkstyle analysis for all classes"
	sourceSets.map { "checkstyle" + it.name.replaceFirstChar(Char::titlecase) }.forEach(::dependsOn)
}