import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

buildscript {
	dependencies {
		classpath("""org.springframework.boot:spring-boot-starter:${property("spring-boot.version")}""")
	}
}

plugins {
	id("io.cornerstone.java-common-conventions")
	id("org.springframework.boot")
	war
}

dependencies {
	implementation(project(":cornerstone-user"))
	implementation(project(":cornerstone-loginrecord"))
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat") // if war not jar
	runtimeOnly("com.mysql:mysql-connector-j")
}

springBoot {
	buildInfo()
}

tasks.withType<BootBuildImage> {
	createdDate = "now"
}

val kustomize = tasks.register<Copy>("kustomize") {
	val cfg = mutableMapOf<String, String>()
	val file = File(projectDir, "src/main/resources/application.yml")
	org.springframework.boot.env.YamlPropertySourceLoader()
		.load(file.name, org.springframework.core.io.FileSystemResource(file)).forEach { ps ->
			val source = ps.source
			if (source is Map<*, *>) {
				(source["spring.config.activate.on-cloud-platform"] ?: "").takeIf {
					it == "" || it.toString().lowercase() == "kubernetes"
				}?.run {
					source.forEach { cfg[it.key.toString()] = it.value.toString() }
				}
			}
		}
	val application = mapOf("name" to (cfg["spring.application.name"] ?: name), "version" to version)
	val server = mapOf("port" to (cfg["server.port"] ?: 8080))
	val management = mapOf(
		"server" to mapOf("port" to (cfg["management.server.port"] ?: server["port"])),
		"endpoints" to mapOf("web" to mapOf("basePath" to (cfg["management.endpoints.web.base-path"] ?: "/actuator")))
	)
	val variables = mapOf("application" to application, "server" to server, "management" to management)
	from(layout.projectDirectory.dir("k8s")) {
		expand(variables)
	}
	into(layout.buildDirectory.dir("k8s"))
	outputs.upToDateWhen { false } // disable UP-TO-DATE to force run every time

}

tasks.register<Exec>("applyKustomization") {
	dependsOn(kustomize)
	workingDir = layout.buildDirectory.dir("k8s").get().asFile
	commandLine = listOf("kubectl", "apply", "-k", ".")
}
