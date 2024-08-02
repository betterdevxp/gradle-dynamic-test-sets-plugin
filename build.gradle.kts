plugins {
    id("groovy")
    id("java-gradle-plugin")
    id("idea")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
    kotlin("jvm")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api("org.unbroken-dome.gradle-plugins:gradle-testsets-plugin:4.1.0")
    implementation(kotlin("stdlib-jdk8"))
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

val pluginDescription = "Opinionated, convention over configuration test plugin. Using https://github.com/unbroken-dome/gradle-testsets-plugin " +
        "for the heavy lifting, this plugin inspects the source trees of a project and automatically configures " +
        "gradle-testsets-plugin based on directory naming conventions."

pluginBundle {
    website = "https://github.com/betterdevxp/gradle-dynamic-test-sets-plugin"
    vcsUrl = "https://github.com/betterdevxp/gradle-dynamic-test-sets-plugin.git"
    tags = listOf("gradle", "testing", "testsets", "test sets", "component test", "integration test", "functional test")
    description = pluginDescription
}

gradlePlugin {
    plugins {
        create("dynamicTestSets") {
            id = "org.betterdevxp.dynamic-test-sets"
            displayName = "Dynamic TestSets"
            description = pluginDescription
            implementationClass = "org.betterdevxp.gradle.test.DynamicTestSetsPlugin"
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
