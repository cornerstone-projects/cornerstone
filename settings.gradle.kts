pluginManagement {
	repositories {
		val repoUrlPrefix: String? by settings
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
		}
	}
}

rootProject.name = "cornerstone"
rootDir.listFiles(File::isDirectory)?.map(File::getName)?.filter { it.startsWith(rootProject.name) || it == "showcase" }
	?.forEach(::include)
