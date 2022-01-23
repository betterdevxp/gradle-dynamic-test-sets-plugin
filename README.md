[![main Actions Status](https://github.com/betterdevxp/gradle-dynamic-test-sets-plugin/workflows/gradle-build/badge.svg)](https://github.com/betterdevxp/gradle-dynamic-test-sets-plugin/actions)

# Gradle Dynamic TestSets Plugin

Gradle plugin that dynamically adds test configurations based on source directory names.

Opinionated, convention over configuration Gradle plugin.  Using the excellent 
[Gradle TestSets plugin](https://github.com/unbroken-dome/gradle-testsets-plugin) for the heavy lifting, this plugin inspects 
the source trees of a project and automatically configures the TestSets plugin based on directory naming conventions.

### Installation

Apply the plugin using standard gradle convention

plugins {
    id ("org.betterdevxp.dynamic-test-sets") version "0.1.0"
}

### Usage

When the plugin is applied, all source directories (src/*) will be inspected and any directory ending in *Test* will 
be recognized as a test source dir and will be automatically added as a test set.

For example, assuming the following source directories

* src/main
* src/test
* src/componentTest
* src/loadTest

Four test sets will be created - `unitTest`, `componentTest`, `loadTest`, and `sharedTest`.  The first is always created
by the TestSets plugin to represent the source set defined by the Java plugin.  The second and third are created by this
plugin based on the source directories defined.  The last is also created by this plugin, regardless of whether a 
corresponding source directory exists, for the purpose of sharing code and dependencies.  If you were to add source files 
to src/sharedTest, they would be compiled and available to all other test sets.

Given the above source directories, here is an example dependencies block...

```gradle
dependencies {
    // available to all test sets
    sharedTestApi "org.spockframework:spock-core:1.3-groovy-2.5"
    
    // unit test dependencies
    testImplementation "com.statemachinesystems:mock-clock:1.0"
    
    // component test dependencies
    componentTestImplementation "com.github.tomakehurst:wiremock-standalone:2.27.2"
    
    // load test dependencies
    loadTestImplementation "org.codehaus.groovy.modules.http-builder:http-builder:0.7.1"
}
```

### Extension

There are two configuration options for this plugin - defining the test order and defining commit stage tests.  The 
example below demonstrates how to configure these, as well as the default values.

```gradle
dynamicTestSets {
    standardTestTaskOrder = ["test", "componentTest", "integrationTest", "functionalTest"]
    commitStageTestTaskNames = ["componentTest"]
}
```
