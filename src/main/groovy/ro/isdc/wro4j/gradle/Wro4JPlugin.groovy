package ro.isdc.wro4j.gradle

import org.apache.commons.lang.StringUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.RelativePath
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet

class Wro4JPlugin implements Plugin<Project> {
    private Copy prepareAssets
    private Copy processWebResources
    private Copy processWebTestResources

    @Override
    public void apply(Project project) {
        def javaConvention = project.convention.findPlugin(JavaPluginConvention)
        if (javaConvention == null) {
            throw new GradleException("wro4j requires java plugin to be applied first")
        }

        def webResources = project.extensions.create(WebResourceSet.NAME, WebResourceSet, project)
        def webjarsRuntime = project.configurations.create("webjarsRuntime")
        def webjars = project.configurations
                .create("webjars")
                .extendsFrom(webjarsRuntime)
        def webjarsTest = project.configurations.create("webjarsTest")

        /* Only webjarsRuntime will be included in final dependencies */
        project.configurations
                .getByName("runtime")
                .extendsFrom(webjarsRuntime)

        /* webjars and webjarsTest are included in testCompile to allow IDEs (like IntelliJ IDEA) index js/css sources */
        project.configurations
                .getByName("testCompile")
                .extendsFrom(webjars, webjarsTest)

        prepareAssets = project.tasks.create("prepareAssets", Copy)
        processWebResources = project.tasks.create("processWebResources", Copy)
        processWebResources.dependsOn prepareAssets
        processWebTestResources = project.tasks.create("processWebTestResources", Copy)

        project.tasks
                .getByName("classes")
                .dependsOn processWebResources

        project.tasks
                .getByName("testClasses")
                .dependsOn processWebTestResources

        project.afterEvaluate {
            configureTasks(webResources, project, javaConvention)
        }
    }

    private void configureTasks(WebResourceSet webResources, Project project, JavaPluginConvention javaConvention) {
        def srcMain = javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        def srcMainDir = webResources.srcMainDir
        def srcTestDir = webResources.srcTestDir
        def buildMainDir = webResources.buildMainDir
        def buildTestDir = webResources.buildTestDir
        def dstDir = new File(srcMain.output.resourcesDir, webResources.dstStaticFolder)

        buildMainDir.mkdirs()
        buildTestDir.mkdirs()

        /* Configure processWebResources task */
        prepareAssets.with {
            from srcMainDir
            into buildMainDir
        }
        processWebResources.dependsOn prepareAssets

        def webjars = project.configurations.getByName("webjars")
        def prepareWebjars = project.tasks.create("prepareWebjars", Copy)
        prepareWebjars.with {
            from (webjars.collect { project.zipTree(it) }) {
                eachFile { unwrapWebjar(it) }
            }
            into buildMainDir
        }
        processWebResources.dependsOn prepareWebjars

        webResources.bundles.each { bundle ->
            def compileWeb = project.tasks.create(nameFor("compileWeb", bundle.name), WebCompileTask)
            compileWeb.with {
                it.bundle = bundle
                sourcesDir = buildMainDir
                outputDir = dstDir

                dependsOn prepareAssets, prepareWebjars
            }
            processWebResources.dependsOn compileWeb
        }

        processWebResources.with {
            from new File(buildMainDir, webResources.srcStaticFolder)
            into dstDir
        }
        if (webResources.mainAssets != null) {
            processWebResources.with webResources.mainAssets.from(buildMainDir)
        }
        /* end of processWebResources task */

        /* Configure processWebTestResources task */
        def webjarsTest = project.configurations.getByName("webjarsTest")
        def prepareWebjarsTest = project.tasks.create("prepareWebjarsTest", Copy)
        prepareWebjarsTest.with {
            from (webjarsTest.collect { project.zipTree(it) }) {
                eachFile { unwrapWebjar(it) }
            }
            into buildTestDir
        }

        processWebTestResources.with {
            into buildTestDir

            dependsOn prepareAssets, prepareWebjarsTest
        }
        if (webResources.testAssets != null) {
            processWebTestResources.with webResources.testAssets.from(srcTestDir)
        } else {
            processWebTestResources.from(srcTestDir)
        }
        /* end of processWebTestResources task */
    }

    private static String nameFor(String prefix, String specName) {
        def name = new StringBuilder(prefix)
        specName.split("\\.|-|_").each { word ->
            name.append(StringUtils.capitalize(word))
        }
        return name.toString()
    }

    private static void unwrapWebjar(FileCopyDetails file) {
        def segments = file.relativePath.segments;
        def index = segments.findIndexOf { StringUtils.equalsIgnoreCase(it, "webjars") }
        if (index > 0) {
            file.relativePath = new RelativePath(
                    file.relativePath.isFile(),
                    Arrays.copyOfRange(segments, index, segments.length)
            )
        }
    }
}
