package org.betterdevxp.gradle.test

import org.betterdevxp.gradle.api.SourceSetAccessor
import org.gradle.api.*
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.unbrokendome.gradle.plugins.testsets.TestSetsPlugin
import org.unbrokendome.gradle.plugins.testsets.dsl.TestLibrary
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets
import java.io.FileFilter

class DynamicTestSetsPlugin: Plugin<Project>, SourceSetAccessor {

    override lateinit var project: Project
        private set
    private lateinit var extension: DynamicTestSetsExtension
        private set

    override fun apply(project: Project) {
        this.project = project
        this.extension = project.extensions.create(DynamicTestSetsExtension.NAME, DynamicTestSetsExtension::class.java)

        project.pluginManager.apply(TestSetsPlugin::class.java)

        configureDynamicTestSets()
        configureTestTaskOrderAndCommitStageDependencies()
    }

    private fun configureDynamicTestSets() {
        val sharedTestSet = configureSharedTestSetLibrary()

        val availableTestSetNames = getAvailableTestSetNames()
        availableTestSetNames.remove("sharedTest")
        availableTestSetNames.forEach {
            configureTestSet(it, sharedTestSet)
        }
    }

    private fun getAvailableTestSetNames(): MutableSet<String> {
        // imports the shared test set libraries to the 'test' test set - https://github.com/unbroken-dome/gradle-testsets-plugin#predefined-unit-test-set
        val availableTestSetNames = mutableSetOf("unitTest")

        project.file("src").listFiles(FileFilter {
            it.isDirectory && it.name.endsWith("Test")
        })?.forEach {
            availableTestSetNames.add(it.name)
        }
        return availableTestSetNames
    }

    private fun configureSharedTestSetLibrary() : TestLibrary {
        val sharedTest = project.testSets.createLibrary("sharedTest")

        val mainSourceSet = getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val sharedTestSourceSet = getSourceSets().getByName("sharedTest")

        sharedTestSourceSet.apiConfigurationName
        sharedTestSourceSet.runtimeClasspathConfigurationName

        with(project.configurations) {
            val sharedTestApiConfiguration = getByName(sharedTestSourceSet.apiConfigurationName)

            sharedTestApiConfiguration.extendsFrom(getByName(mainSourceSet.implementationConfigurationName))
            sharedTestApiConfiguration.dependencies.add(project.dependencies.create(mainSourceSet.output))
            project.plugins.withType(JavaLibraryPlugin::class.java) {
                sharedTestApiConfiguration.extendsFrom(getByName(mainSourceSet.apiConfigurationName))
            }

            getByName(sharedTestSourceSet.runtimeClasspathConfigurationName).extendsFrom(getByName(mainSourceSet.runtimeClasspathConfigurationName))
        }

        return sharedTest
    }

    private fun configureTestSet(testSetName: String, sharedTest: TestLibrary) {
        project.testSets {
            testSetName {
                imports(sharedTest)
            }
        }
    }

    private fun configureTestTaskOrderAndCommitStageDependencies() {
        project.afterEvaluate {
            val standardTestTaskOrderTasks = getStandardTestTaskProviders()
            for (i in standardTestTaskOrderTasks.size - 1 downTo 1) {
                standardTestTaskOrderTasks.subList(0, i).forEach { provider: TaskProvider<Task> ->
                    standardTestTaskOrderTasks[i].configure {
                        it.mustRunAfter(provider)
                    }
                }
            }

            val availableTestSetNames = getAvailableTestSetNames()
            project.tasks.named("check").configure { task: Task ->
                extension.commitStageTestTaskNames.forEach {
                    if (availableTestSetNames.contains(it)) {
                        task.dependsOn(it)
                    }
                }
            }
        }
    }

    private fun getStandardTestTaskProviders(): List<TaskProvider<Task>>  {
        val standardTestTaskOrderTasks = extension.standardTestTaskOrder.mapNotNull {
            try {
                project.tasks.named(it)
            } catch (ex: UnknownTaskException) {
                null
            }
        }
        return standardTestTaskOrderTasks
    }

}