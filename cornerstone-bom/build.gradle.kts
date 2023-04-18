plugins {
	id("io.cornerstone.java-library-conventions")
}

dependencies {
	constraints {
		rootProject.subprojects.filter { it.name != "showcase" && !it.name.endsWith("-bom") }.forEach {
			api("${it.group}:${it.name}:${it.version}")
		}
	}
}
