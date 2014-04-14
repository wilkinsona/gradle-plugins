package org.springframework.build.gradle.springio.platform

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.internal.artifacts.DefaultModuleVersionSelector
import org.gradle.api.internal.artifacts.ivyservice.DefaultDependencyResolveDetails
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 *
 * @author Rob Winch
 */
class IncompleteExcludesTaskTests extends Specification {
	Project parent
	Task task

	def setup() {
		parent = ProjectBuilder.builder().withName("parent").build()
		parent.apply plugin: JavaPlugin
		task = parent.tasks.create(name: SpringioPlatformPlugin.INCOMPLETE_EXCLUDES_TASK_NAME, type: IncompleteExcludesTask)
	}

	def "fails with exclude group only"() {
		setup:
			parent.dependencies {
				compile('org.springframework:spring-core:3.2.0.RELEASE') {
					exclude group: 'commons-logging'
				}
			}
		when:
			task.check()
		then:
			thrown(IllegalStateException)
	}

	def "fails with exclude module only"() {
		setup:
			parent.dependencies {
				compile('org.springframework:spring-core:3.2.0.RELEASE') {
					exclude module: 'commons-logging'
				}
			}
		when:
			task.check()
		then:
			thrown(IllegalStateException)
	}

	def "succeeds with both group and module"() {
		setup:
			parent.dependencies {
				compile('org.springframework:spring-core:3.2.0.RELEASE') {
					exclude group: 'commons-logging', module: 'commons-logging'
				}
			}
		when:
			task.check()
		then:
			noExceptionThrown()
	}
}