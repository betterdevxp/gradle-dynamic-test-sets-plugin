package org.betterdevxp.gradle.testkit

trait ProjectFileSystem {

    TestFile projectDir = new TestFile("build/gradlePluginTestOutputDir/${UUID.randomUUID()}")

    TestFile projectFile(String path) {
        new TestFile(projectDir, path)
    }

    TestFile getBuildDir() {
        projectFile("build")
    }

    TestFile getBuildFile() {
        projectFile("build.gradle")
    }

    TestFile getGradlePropertiesFile() {
        projectFile("gradle.properties")
    }

    TestFile getSettingsFile() {
        projectFile("settings.gradle")
    }

}

