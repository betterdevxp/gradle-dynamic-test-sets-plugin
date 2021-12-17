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

project.tasks.withType(Test) {
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

    def "should make classes from main source set available to test sets"() {
        given:
        buildFile << """
        dependencies {
            mainTestApi 'org.spockframework:spock-core:1.3-groovy-2.5'
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
        and: "class defined in the 'mainTest' test set library"
        projectFile("src/mainTest/groovy/MainTestUtils.groovy") << """
class MainTestUtils {
    static void printLine(String line) {
        Utils.printLine(line)
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
        MainTestUtils.printLine("main test output")
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
        assert result.output.contains("main test output")
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
    mainTestApi 'org.spockframework:spock-core:1.3-groovy-2.5'
}
"""
        projectFile("src/mainTest/groovy/MainTestUtils.groovy") << """
class MainTestUtils {
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

    def "should automatically run component tests but not other test sets as part of check"() {
        given: "a component test that prints a string"
        projectFile("src/componentTest/groovy/ComponentTest.groovy") << """
class ComponentTest extends spock.lang.Specification {
    def "component test"() {
        when:
        println "run component test"
        
        then:
        true
    }
}
"""
        and: "an integration test that prints a string"
        projectFile("src/integrationTest/groovy/IntegrationTest.groovy") << """
class IntegrationTest extends spock.lang.Specification {
    def "integration test"() {
        when:
        printLine "run integration test"
        
        then:
        true
    }
}
"""

        when:
        BuildResult result = run("check")

        then: "only the component test should have executed"
        assert result.output.contains("run component test")
        assert result.output.contains("run integration test") == false
    }

}