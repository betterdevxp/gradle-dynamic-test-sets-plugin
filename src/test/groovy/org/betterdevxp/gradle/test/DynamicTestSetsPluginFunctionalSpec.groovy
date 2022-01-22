package org.betterdevxp.gradle.test

import org.betterdevxp.gradle.testkit.GradleRunnerSupport
import org.gradle.testkit.runner.BuildResult
import spock.lang.Specification

class DynamicTestSetsPluginFunctionalSpec extends Specification implements GradleRunnerSupport {

    def setup() {
        buildFile << """
plugins {
    id 'groovy'
    id 'org.betterdevxp.dynamic-test-sets'
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // this dependency will be available to all test sets, so each test will have access to spock
    sharedTestApi 'org.spockframework:spock-core:1.3-groovy-2.5'
}

project.tasks.withType(Test).configureEach {
    // allows us to assert against test output
    testLogging.showStandardStreams = true
}
"""
    }

    def "should create sharedTest test set library and automatically import into unit test set"() {
        given: "class file in sharedTest source set"
        projectFile("src/sharedTest/groovy/Utils.groovy") << """
class Utils {
    static void printLine(String line) {
        println line
    } 
}
"""
        and: "unit test which should have access to class file defined in sharedTest"
        projectFile("src/test/groovy/SomeTest.groovy") << """
class SomeTest extends spock.lang.Specification {

    def "some test"() {
        when:
        Utils.printLine("expect this output")
        
        then:
        true
    }
}
"""
        when:
        BuildResult result = run("test")

        then:
        assert result.output.contains("expect this output")
    }

    def "should add test set dynamically based on source directory name"() {
        given: "existence of componentTest directory should create the componentTestCompile configuration"
        buildFile << """
dependencies {
    componentTestCompile "com.google.guava:guava:26.0-jre"
}
"""
        and: "class defined in the dynamically added configuration should have access to declared dependencies"
        projectFile("src/componentTest/groovy/SomeTest.groovy") << """
class SomeTest extends spock.lang.Specification {

    def "some test"() {
        when:
        println "expect this output"
        
        then:
        true
    }
}
"""
        when:
        BuildResult result = run("componentTest")

        then:
        assert result.output.contains("expect this output")
    }

    def "should make classes from main source set available to shared test set"() {
        given:
        buildFile << """
        dependencies {
            sharedTestApi 'org.spockframework:spock-core:1.3-groovy-2.5'
        }
"""

        and: "class defined in the 'main' source set"
        projectFile("src/main/java/Utils.java") << """
public class Utils {
    public static void printLine(String line) {
        System.out.println(line);
    } 
}
"""
        and: "class defined in the 'sharedTest' test set library"
        projectFile("src/sharedTest/groovy/SharedTestUtils.groovy") << """
class SharedTestUtils {
    static void printLine(String line) {
        Utils.printLine(line)
    } 
}
"""
        and: "test declared in the dynamically created 'componentTest' test set should have access to all of the above"
        projectFile("src/componentTest/groovy/SomeTest.groovy") << """
class SomeTest extends spock.lang.Specification {

    def "some test"() {
        when:
        SharedTestUtils.printLine("shared test output")
        Utils.printLine("main source set output")
        
        then:
        true
    }
}
"""
        when:
        BuildResult result = run("componentTest")

        then:
        assert result.output.contains("shared test output")
        assert result.output.contains("main source set output")
    }

    def "should make compile dependencies available to test library configurations"() {
        given:
        buildFile << """
dependencies {
    compile "com.google.guava:guava:26.0-jre"
}
"""
        projectFile("src/sharedTest/groovy/SharedTestUtils.groovy") << """
class SharedTestUtils {
    static void shouldCompile() {
        com.google.common.collect.ArrayListMultimap.create();
    } 
}
"""

        when:
        run("build")

        then:
        notThrown(Exception)
    }

    def "should make api dependencies available to test library configurations when java-library plugin is applied"() {
        given:
        buildFile << """
apply plugin: "java-library"

dependencies {
    api "com.google.guava:guava:26.0-jre"
    sharedTestApi 'org.spockframework:spock-core:1.3-groovy-2.5'
}
"""
        projectFile("src/sharedTest/groovy/SharedTestUtils.groovy") << """
class SharedTestUtils {
    static void shouldCompile() {
        com.google.common.collect.ArrayListMultimap.create();
    } 
}
"""

        when:
        run("build")

        then:
        notThrown(Exception)
    }

    def "should not eagerly create test tasks"() {
        given:
        String realizedTestTaskPrefix = "TEST TASK"
        // not sure of a better way to test this... ideally, could determine whether the task has been realized in a
        // unit test but haven't been able to figure that out
        buildFile << """
project.tasks.withType(Test).configureEach {
    println "${realizedTestTaskPrefix} - \${it}"
}

task nonTestTask {}
"""

        and:
        projectFile("src/functionalTest/groovy/SomeTest.groovy") << """
class SomeTest {}
"""

        when:
        BuildResult result = run("nonTestTask")

        then:
        assert result.output.contains(realizedTestTaskPrefix) == false
    }

}
