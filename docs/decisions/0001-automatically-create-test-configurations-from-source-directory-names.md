# Automatically derive test configurations from source directory names

## Context and Problem Statement

The gradle java plugin provides the 'test' source set (and accompanying tasks) which traditionally houses unit tests.
Many projects will define additional test source sets - component, integration, functional, load, performance, etc.
How can we allow for various test configurations with minimal duplication?

## Considered Options

* Automatic derivation from source directory names
* Create plugin for each type of desired test
* Define each test source set/configuration/task within each project

## Decision Outcome

Chosen option: "Automatic derivation from source directory names", because

* Test source sets generally follow a strict naming convention, namely *Test
* Reduces maintenance burden of multiple plugins
* Reduces duplication across build scripts
