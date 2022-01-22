package org.betterdevxp.gradle.test

import org.betterdevxp.gradle.testkit.ProjectSupport
import spock.lang.Specification

class DynamicTestSetsPluginSpec extends Specification implements ProjectSupport {

    void applyDynamicTestSetsPlugin() {
        applyPlugin(DynamicTestSetsPlugin)
    }

    private void createSrcDirs(String... names) {
        names.each { String name ->
            projectFile("src/${name}").mkdirs()
        }
    }

    def "should automatically run component tests but not other test sets as part of check, by default"() {
        given:
        createSrcDirs("componentTest", "integrationTest")
        applyDynamicTestSetsPlugin()

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskDependency("check", "componentTest")
        projectValidator.assertNoTaskDependency("check", "integrationTest")
    }

    def "should automatically run tasks defined in commitStageTestTaskNames extension"() {
        given:
        createSrcDirs("componentTest", "integrationTest", "functionalTest")
        applyDynamicTestSetsPlugin()

        and:
        project.extensions.findByType(DynamicTestSetsExtension).commitStageTestTaskNames = ["integrationTest", "functionalTest"]

        when:
        evaluateProject()

        then:
        projectValidator.assertNoTaskDependency("check", "componentTest")
        projectValidator.assertTaskDependency("check", "integrationTest")
        projectValidator.assertTaskDependency("check", "functionalTest")
    }

    def "should configure test order according to extension"() {
        given:
        createSrcDirs("componentTest", "intTest", "functionalTest")
        applyDynamicTestSetsPlugin()

        and:
        project.extensions.findByType(DynamicTestSetsExtension).standardTestTaskOrder = ["test", "functionalTest", "intTest"]

        when:
        evaluateProject()

        then:
        projectValidator.assertTaskMustRunAfter("functionalTest", "test")
        projectValidator.assertTaskMustRunAfter("intTest", "test")
        projectValidator.assertTaskMustRunAfter("intTest", "functionalTest")
        projectValidator.assertTaskMustRunAfterNotDefined("componentTest", "test")
    }

}
