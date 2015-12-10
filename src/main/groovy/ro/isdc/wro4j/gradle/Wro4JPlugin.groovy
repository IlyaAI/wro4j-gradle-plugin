package ro.isdc.wro4j.gradle

import org.apache.commons.lang.StringUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet

class Wro4JPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        def javaConvention = project.convention.findPlugin(JavaPluginConvention)
        if (javaConvention == null) {
            throw new GradleException("wro4j requires java plugin to be applied first")
        }

        def webResources = project.extensions.create(WebResourceSet.NAME, WebResourceSet, project)
        project.configurations.create("webjars")

        project.afterEvaluate {
            createTasks(webResources, project, javaConvention)
        }
    }

    private static void createTasks(WebResourceSet webResources, Project project, JavaPluginConvention javaConvention) {
        def srcMain = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        def srcRoot = webResources.srcDir
        def model = webResources.createWroModel()
        def dstRoot = new File(srcMain.output.resourcesDir, webResources.staticFolder)

        def processWebResources = project.tasks.create("processWebResources")

        webResources.bundles.each { bundle ->
            def compileWeb = project.tasks.create(nameFor("compileWeb", bundle.name), WebCompileTask)
            compileWeb.with {
                wroModel = model
                targetGroups = [bundle.name]
                preProcessors = bundle.preProcessors
                postProcessors = bundle.postProcessors
                sourcesDir = srcRoot
                outputDir = dstRoot
            }
            processWebResources.dependsOn compileWeb
        }

        def copyStatic = project.tasks.create(nameFor("copy", webResources.staticFolder), Copy)
        copyStatic.with {
            from srcRoot
            include "${webResources.staticFolder}/**"
            into dstRoot
        }
        processWebResources.dependsOn copyStatic

        def copyAssets = project.tasks.create("copyAssets", Copy)
        copyAssets.with {
            into dstRoot
            with webResources.assetsSpec
        }
        processWebResources.dependsOn copyAssets

        project.tasks.getByName("classes")
                .dependsOn processWebResources
    }

    private static String nameFor(String prefix, String specName) {
        def name = new StringBuilder(prefix)
        specName.split("\\.|-|_").each { word ->
            name.append(StringUtils.capitalize(word))
        }
        return name.toString()
    }
}
