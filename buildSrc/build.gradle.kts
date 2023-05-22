import java.util.Properties
import java.io.FileInputStream

plugins {
	`kotlin-dsl`
}

FileInputStream("$rootDir/../gradle.properties").use {
	Properties().apply { load(it) }.forEach {
		val key = it.key as String
		if (!hasProperty(key))
			extra[key] = it.value
	}
}

repositories {
	val repoUrlPrefix: String? by project
	if (repoUrlPrefix != null) {
		maven {
			url = uri("${repoUrlPrefix}/maven-public/")
			isAllowInsecureProtocol = true
		}
		maven {
			url = uri("${repoUrlPrefix}/gradle-plugins/")
			isAllowInsecureProtocol = true
		}
	} else {
		gradlePluginPortal()
		mavenCentral()
		maven {
			url = uri("https://repo.spring.io/milestone")
		}
	}
}

dependencies {
	implementation("""io.freefair.lombok:io.freefair.lombok.gradle.plugin:${property("lombok-plugin.version")}""")
	implementation("""io.spring.gradle:dependency-management-plugin:${property("dependency-management-plugin.version")}""")
	implementation("""io.spring.javaformat:spring-javaformat-gradle-plugin:${property("javaformat-plugin.version")}""")
	implementation("""org.springframework.boot:org.springframework.boot.gradle.plugin:${property("spring-boot.version")}""")
}
