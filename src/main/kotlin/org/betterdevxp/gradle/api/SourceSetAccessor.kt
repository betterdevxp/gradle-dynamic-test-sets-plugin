package org.betterdevxp.gradle.api

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GradleVersion

interface SourceSetAccessor {

    val project: Project

    fun getSourceSets(): SourceSetContainer {
        return if (GradleVersion.version(project.gradle.gradleVersion) < GradleVersion.version("7.1")) {
            getSourceSetsFromConvention()
        } else {
            getSourceSetsFromExtension()
        }
    }

    private fun getSourceSetsFromExtension(): SourceSetContainer {
        return project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
    }

    private fun getSourceSetsFromConvention() : SourceSetContainer {
        return project.convention.getPlugin(JavaPluginConvention::class.java).sourceSets
    }

}