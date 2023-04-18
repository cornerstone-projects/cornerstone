plugins {
	id("io.cornerstone.java-common-conventions")
	`java-library`
	`maven-publish`
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			versionMapping {
				usage("java-api") {
					val hasTestFixtures = configurations.any { it.name == "testFixturesRuntimeClasspath" }
					fromResolutionOf(if (hasTestFixtures) "testFixturesRuntimeClasspath" else "runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}
			suppressAllPomMetadataWarnings()
		}
	}
	val repoUrlPrefix: String? by project
	if (repoUrlPrefix != null) {
		repositories {
			val version: String by project
			val repoUser: String? by project
			val repoPassword: String? by project
			maven {
				if (version.endsWith("-SNAPSHOT")) {
					url = uri("${repoUrlPrefix}/maven-snapshots/")
				} else {
					url = uri("${repoUrlPrefix}/maven-releases/")
				}
				isAllowInsecureProtocol = true
				credentials {
					username = repoUser
					password = repoPassword
				}
			}
		}
	}
}
