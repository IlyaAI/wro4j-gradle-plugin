package ro.isdc.wro4j.gradle

import org.apache.commons.lang.StringUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import ro.isdc.wro4j.gradle.tasks.WebCompileTask;

class Wro4JPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        def javaConvention = project.convention.findPlugin(JavaPluginConvention)
        if (javaConvention == null) {
            throw new GradleException("wro4j requires java plugin to be applied first")
        }

        def webResources = project.extensions.create(WebResourceSet.NAME, WebResourceSet, project)
        project.extensions.extraProperties.set("WebCompile", WebCompileTask)
        project.configurations.create("webjar")

        project.afterEvaluate {
            createTasks(project, javaConvention, webResources)
        }
    }

    private static void createTasks(Project project, JavaPluginConvention javaConvention, WebResourceSet webResources) {
        def sourceSetMain = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        def srcRoot = webResources.srcDir
        def srcWro = new File(srcRoot, "wro.xml")
        def dstRoot = new File(sourceSetMain.output.resourcesDir, "static")

        def processWebResources = project.tasks.create("processWebResources")

        webResources.jsSpecs.each {spec ->
            def compileJs = project.tasks.create(getTaskName("compileJs", spec.name), WebCompileTask)
            compileJs.with {
                wroFile = srcWro
                targetGroups = spec.targetGroups
                preProcessors = spec.preProcessors
                postProcessors = spec.postProcessors
                sourcesDir = srcRoot
                outputDir = dstRoot
            }
            processWebResources.dependsOn compileJs
        }

        webResources.themeSpecs.each {spec ->
            def compileTheme = project.tasks.create(getTaskName("compileTheme", spec.name), WebCompileTask)
            compileTheme.with {
                wroFile = srcWro
                targetGroups = spec.targetGroups
                preProcessors = spec.preProcessors
                postProcessors = spec.postProcessors
                sourcesDir = srcRoot
                outputDir = dstRoot
            }
            processWebResources.dependsOn compileTheme
        }

        def copyThemeAssets = project.tasks.create("copyThemeAssets", Copy)
        copyThemeAssets.with {
            from project.fileTree(new File(srcRoot, "themes")) {
                include "**"
            }
            into new File(dstRoot, "themes")
            exclude "**/*.css", "**/*.less", "**/*.saas", "**/*.js"
        }
        processWebResources.dependsOn copyThemeAssets

        def copyStaticAssets = project.tasks.create("copyStaticAssets", Copy)
        copyStaticAssets.with {
            from project.fileTree(new File(srcRoot, "static")) {
                include "**"
            }
            into dstRoot
        }
        processWebResources.dependsOn copyStaticAssets

        project.tasks.getByName("classes")
                .dependsOn processWebResources
    }

    private static String getTaskName(String prefix, String specName) {
        def name = new StringBuilder(prefix)
        specName.split("\\.|-|_").each {word ->
            name.append(StringUtils.capitalize(word))
        }
        return name.toString()
    }
}
