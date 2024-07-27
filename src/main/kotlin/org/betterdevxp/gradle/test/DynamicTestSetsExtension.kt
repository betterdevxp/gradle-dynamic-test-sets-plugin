package org.betterdevxp.gradle.test

open class DynamicTestSetsExtension {

    companion object {
        const val NAME = "dynamictestsets"
    }

    /**
     * If defined, these tasks will be executed in index order
     */
    var standardTestTaskOrder: List<String> = listOf("test", "componentTest", "integrationTest", "functionalTest")

    /**
     * These tasks will be automatically added as 'check' task dependencies
     */
    var commitStageTestTaskNames: List<String> = listOf("componentTest")
}