plugins {
	id("io.cornerstone.java-library-conventions")
}

dependencies {
	api(project(":cornerstone-core"))
	api("""commons-net:commons-net:${property("commons-net.version")}""")
	api("""com.amazonaws:aws-java-sdk-s3:${property("aws-sdk-java.version")}""")
}
