# Automatically create 'sharedTest' source set

## Context and Problem Statement

When a project has multiple test configurations, there is often a need to share code and dependencies between 
them (e.g. builders/fixtures, test frameworks, etc).
How to best support this without impacting projects without that need?

## Considered Options

* Do nothing, projects can declare their own 'common' test set if need be
* Use a [version catalog](https://docs.gradle.org/current/userguide/platforms.html)
* Use provided 'test' source set to shared code and dependencies
* Define a custom test set library which all dynamically created test sets import

## Decision Outcome

Chosen option: "Define a custom test set library which all dynamically created test sets import", because 

* Having projects declare their own test set would likely result in duplication and mistakes
* Use of a version catalog only solves for the dependency issue and may still introduce significant duplication
* Using the provided 'test' source set is not ideal because we do not want tests and resource files from one test set
  bleeding into others
* Using the provided 'test' source set for dependencies is also not ideal since it's not clear which dependencies are 
  shared and which are specific to unit tests
* Using a common 'shared' test set bring consistency - here is where you always add shared test dependencies.
* Shared code is available to all test configurations without the need for any test configuration to depend on another
