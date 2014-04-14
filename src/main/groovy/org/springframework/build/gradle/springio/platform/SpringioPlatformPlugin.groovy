package org.springframework.build.gradle.springio.platform

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test

/**
 *
 * @author Rob Winch
 */
class SpringioPlatformPlugin implements Plugin<Project> {
	static String CHECK_TASK_NAME = 'springioCheck'
	static String TEST_TASK_NAME = 'springioTest'
	static String INCOMPLETE_EXCLUDES_TASK_NAME = 'springioIncompleteExcludesCheck'
	static String ALTERNATIVE_DEPENDENCIES_TASK_NAME = 'springioAlternativeDependenciesCheck'
	static String CONFIG_RESOLUTION_STRATEGY_TASK_NAME = 'configureSpringioConfiguration'

	@Override
	void apply(Project project) {
		project.plugins.withType(JavaPlugin) {
			applyJavaProject(project)
		}
	}

	def applyJavaProject(Project project) {
		Configuration springioTestRuntimeConfig = project.configurations.create('springioTestRuntime', {
			extendsFrom project.configurations.testRuntime
		})

		ConfigureResolutionStrategyTask configureSpringioTask = project.tasks.create(CONFIG_RESOLUTION_STRATEGY_TASK_NAME, ConfigureResolutionStrategyTask)
		configureSpringioTask.configuration = springioTestRuntimeConfig

		Task springioTest = project.tasks.create(TEST_TASK_NAME)

		['JDK7','JDK8'].each { jdk ->
			maybeCreateJdkTest(project, springioTestRuntimeConfig, jdk, springioTest)
		}

		Task incompleteExcludesCheck = project.tasks.create(INCOMPLETE_EXCLUDES_TASK_NAME, IncompleteExcludesTask)
		Task alternativeDependenciesCheck = project.tasks.create(ALTERNATIVE_DEPENDENCIES_TASK_NAME, AlternativeDependenciesTask)

		project.tasks.create(CHECK_TASK_NAME) {
			dependsOn springioTest
			dependsOn incompleteExcludesCheck
			dependsOn alternativeDependenciesCheck
		}
	}

	private void maybeCreateJdkTest(Project project, Configuration springioTestRuntimeConfig, String jdk, Task springioTest) {
		def whichJdk = "${jdk}_HOME"
		if(!project.hasProperty(whichJdk)) {
			return
		}
		def jdkHome = project."${whichJdk}"
		def exec = new File(jdkHome,'/bin/java')
		if(!exec.exists()) {
			throw new IllegalStateException("The path $exec does not exist! Please ensure to define a valid JDK home as a commandline argument using -P${whichJdk}=<path>")
		}

		Test springioJdkTest = project.tasks.create("springio${jdk}Test", Test)
		project.configure(springioJdkTest) {
			classpath = project.sourceSets.test.output + project.sourceSets.main.output + springioTestRuntimeConfig
			testResultsDir = project.file("$project.buildDir/springio-$jdk-test-results/")
			testReportDir = project.file("$project.buildDir/reports/springio-$jdk-tests/")
		}
		springioTest.dependsOn springioJdkTest
	}
}
