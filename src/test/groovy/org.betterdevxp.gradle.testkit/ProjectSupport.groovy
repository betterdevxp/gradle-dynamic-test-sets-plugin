package org.betterdevxp.gradle.testkit

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

trait ProjectSupport extends ProjectFileSystem {

    private Project theProject

    String getProjectName() {
        "root"
    }

    Project getProject() {
        if (theProject == null) {
            theProject = createProject()
        }
        theProject
    }

    Project createProject() {
        ProjectBuilder.builder()
                .withName("${projectName}-project")
                .withProjectDir(projectDir)
                .build()
    }

    Project createSubProject(String subProjectName) {
        File subProjectDir = projectFile(subProjectName)
        subProjectDir.mkdirs()

        ProjectBuilder.builder()
                .withName(subProjectName)
                .withProjectDir(subProjectDir)
                .withParent(project)
                .build()
    }

    ProjectValidator getProjectValidator() {
        new ProjectValidator(project)
    }

    /**
     * For some reason, evaluate does not show up in IDEA code completion so provide a delegate method
     */
    void evaluateProject() {
        project.evaluate()
    }

}
