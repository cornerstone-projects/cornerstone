configurations.create("optional").apply {
	isCanBeConsumed = false
	isCanBeResolved = false
	val optional = this
	plugins.withType<JavaPlugin> {
		extensions.getByType<JavaPluginExtension>().sourceSets.forEach {
			configurations.getByName(it.compileClasspathConfigurationName).extendsFrom(optional)
			configurations.getByName(it.runtimeClasspathConfigurationName).extendsFrom(optional)
		}
	}
}
