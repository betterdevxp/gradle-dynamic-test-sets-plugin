package org.betterdevxp.gradle.test

import org.betterdevxp.gradle.api.SourceSetAccessor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.unbrokendome.gradle.plugins.testsets.TestSetsPlugin

class DynamicTestSetsPlugin implements Plugin<Project>, SourceSetAccessor {

    private Project project
    private DynamicTestSetsExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create(DynamicTestSetsExtension.NAME, DynamicTestSetsExtension)

        project.pluginManager.apply(TestSetsPlugin)
        configureDynamicTestSets()
        configureTestTaskOrderAndCommitStageDependencies()
    }

    @Override
    Project getProject() {
        project
    }

    private List<String> configureDynamicTestSets() {
        List<String> availableTestSetNames = getAvailableTestSetNames()

        availableTestSetNames.remove("sharedTest")
        configureSharedTestSetLibrary()

        availableTestSetNames.each { String testSetName ->
            configureTestSet(testSetName)
        }
    }

    private List<String> getAvailableTestSetNames() {
        List<String> availableTestSetNames = []
        // imports the shared test set libraries to the 'test' test set - https://github.com/unbroken-dome/gradle-testsets-plugin#predefined-unit-test-set
        availableTestSetNames.add("unitTest")
        project.file("src").listFiles({ File file ->
            file.isDirectory() && file.name.endsWith("Test")
        } as FileFilter).each {
            availableTestSetNames.add(it.name)
        }
        availableTestSetNames
    }

    private void configureSharedTestSetLibrary() {
        project.testSets {
            libraries {
                sharedTest
            }
        }

        SourceSet mainSourceSet = getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        SourceSet libSourceSet = getSourceSets().getByName("sharedTest")

        project.configurations.with {
            it[libSourceSet.apiConfigurationName].extendsFrom(it[mainSourceSet.implementationConfigurationName])
            it[libSourceSet.runtimeClasspathConfigurationName].extendsFrom(it[mainSourceSet.runtimeClasspathConfigurationName])
        }
        project.configurations.findByName(libSourceSet.apiConfigurationName).dependencies.add(
                project.dependencies.create(mainSourceSet.output)
        )

        project.plugins.withType(JavaLibraryPlugin) {
            project.configurations.with {
                it[libSourceSet.apiConfigurationName].extendsFrom(it[mainSourceSet.apiConfigurationName])
            }
        }
    }

    private void configureTestSet(String testSetName) {
        project.testSets {
            "${testSetName}" {
                imports libraries.sharedTest
            }
        }
    }

    private void configureTestTaskOrderAndCommitStageDependencies() {
        project.afterEvaluate {
            List<TaskProvider<Task>> standardTestTaskOrderTasks = getStandardTestTaskProviders()
            for (int i = standardTestTaskOrderTasks.size() - 1; i > 0; i--) {
                standardTestTaskOrderTasks[0..i - 1].each { TaskProvider<Task> provider ->
                    standardTestTaskOrderTasks[i].configure {
                        mustRunAfter(provider)
                    }
                }
            }

            List<String> availableTestSetNames = getAvailableTestSetNames()
            project.tasks.named("check").configure { Task task ->
                extension.commitStageTestTaskNames.each {
                    if (availableTestSetNames.contains(it)) {
                        task.dependsOn(it)
                    }
                }
            }
        }
    }

    private List<TaskProvider<Task>> getStandardTestTaskProviders() {
        List<TaskProvider<Task>> standardTestTaskOrderTasks = extension.standardTestTaskOrder.collect {
            try {
                return project.tasks.named(it)
            } catch (UnknownTaskException ex) {
                return null
            }
        }.findAll {
            it != null
        }
        standardTestTaskOrderTasks
    }

}
