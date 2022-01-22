package org.betterdevxp.gradle.testkit

trait ProjectFileSystem {

    private TestFile theProjectDir = new TestFile("build/gradlePluginTestOutputDir/${UUID.randomUUID()}")

    TestFile getProjectDir() {
        theProjectDir.mkdirs()
        theProjectDir
    }

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

