package org.betterdevxp.gradle.testkit

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.tasks.TaskProvider

class ProjectValidator {

    private Project project

    ProjectValidator(Project project) {
        this.project = project
    }

    void assertPluginApplied(String pluginName) {
        assert project.plugins.getPlugin(pluginName) != null
    }

    List<String> getDependencyNamesForTask(String taskName) {
        Task task = project.tasks.getByName(taskName)
        task.taskDependencies.getDependencies(task).toList().collect { it.name }
    }

    void assertTaskDependency(String taskName, String... expectedDependencyNames) {
        expectedDependencyNames.each { String expectedDependencyName ->
            assertTaskDependency(taskName, expectedDependencyName)
        }
    }

    void assertTaskDependency(String taskName, String expectedDependencyName) {
        List<String> dependencyNames = getDependencyNamesForTask(taskName)
        assert dependencyNames.contains(expectedDependencyName)
        "Expected task ${taskName} to declare dependency on ${expectedDependencyName}, actual dependencies: ${dependencyNames}"
    }

    void assertNoTaskDependency(String taskName, String expectedMissingDependencyName) {
        List<String> dependencyNames = getDependencyNamesForTask(taskName)
        assert !dependencyNames.contains(expectedMissingDependencyName)
        "Expected task ${taskName} to NOT declare dependency on ${expectedMissingDependencyName}, actual dependencies: ${dependencyNames}"
    }

    void assertTasksDefined(String... names) {
        names.each { String name ->
            assert project.tasks.findByName(name)
        }
    }

    void assertTasksNotDefined(String... names) {
        names.each { String name ->
            assert !project.tasks.findByName(name)
        }
    }

    void assertTaskMustRunAfter(String taskName, String expectedMustRunAfterName) {
        assert getMustRunAfterTaskNames(taskName).contains(expectedMustRunAfterName)
    }

    void assertTaskMustRunAfterNotDefined(String taskName, String expectedMustRunAfterName) {
        assert getMustRunAfterTaskNames(taskName).contains(expectedMustRunAfterName) == false
    }

    private List<String> getMustRunAfterTaskNames(String taskName) {
        Task task = project.tasks.getByName(taskName)
        List<String> mustRunAfterTaskNames = (task.getMustRunAfter() as DefaultTaskDependency).getMutableValues().collect {
            if (it instanceof String) {
                return it
            } else if (it instanceof Task) {
                return it.name
            } else if (it instanceof TaskProvider) {
                return it.name
            } else {
                throw new IllegalStateException("Unknown value=${it} of type=${it.class}")
            }
        }
        mustRunAfterTaskNames
    }

}
