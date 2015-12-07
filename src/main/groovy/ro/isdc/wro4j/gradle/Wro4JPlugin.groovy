package ro.isdc.wro4j.gradle

import org.gradle.api.Plugin;
import org.gradle.api.Project
import ro.isdc.wro4j.gradle.tasks.WebCompileTask;

class Wro4JPlugin implements Plugin<Project> {
    WebCompileTask compileWeb;

    @Override
    public void apply(Project project) {
        project.extensions.extraProperties.set(WebCompileTask.getSimpleName(), WebCompileTask)

        compileWeb = project.tasks.create("compileWeb", WebCompileTask)
        compileWeb.targetGroup("main")
        compileWeb.sourcesDir = new File(project.projectDir, "src/main/resources/wro") // TODO: use SourceSet settings here
        compileWeb.outputDir = new File(project.buildDir, "resources/main/static")
    }
}
