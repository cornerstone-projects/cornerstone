import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class OptionalDependenciesPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.configurations.create("optional").apply {
			isCanBeConsumed = false
			isCanBeResolved = false
			val optional = this
			project.plugins.withType<JavaPlugin> {
				project.extensions.getByType<JavaPluginExtension>().sourceSets.forEach {
					project.configurations.getByName(it.compileClasspathConfigurationName).extendsFrom(optional)
					project.configurations.getByName(it.runtimeClasspathConfigurationName).extendsFrom(optional)
				}
			}
		}
	}
}

apply<OptionalDependenciesPlugin>()
