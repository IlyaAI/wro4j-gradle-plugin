package ro.isdc.wro4j.gradle

import org.apache.commons.lang.StringUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin;
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
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

        def model = webResources.createWroModel()
        def srcRoot = webResources.srcDir
        def tmpRoot = new File(project.buildDir, "wro")
        def dstRoot = new File(srcMain.output.resourcesDir, webResources.staticFolder)

        def processWebResources = project.tasks.create("processWebResources", Copy)

        tmpRoot.mkdirs()
        def webjars = project.configurations.getByName("webjars")
        def prepareAssets = project.tasks.create("prepareAssets", Copy)
        prepareAssets.with {
            from srcRoot
            from (webjars.collect { project.zipTree(it) }) {
                eachFile { file ->
                    def segments = file.relativePath.segments;
                    def index = segments.findIndexOf { StringUtils.equalsIgnoreCase(it, "webjars") }
                    if (index > 0) {
                        file.relativePath = new RelativePath(true, Arrays.copyOfRange(segments, index, segments.length))
                    }
                }
            }
            into tmpRoot
        }
        processWebResources.dependsOn prepareAssets
        project.configurations.getByName("runtime").extendsFrom(webjars)

        webResources.bundles.each { bundle ->
            def compileWeb = project.tasks.create(nameFor("compileWeb", bundle.name), WebCompileTask)
            compileWeb.with {
                wroModel = model
                targetGroups = [bundle.name]
                preProcessors = bundle.preProcessors
                postProcessors = bundle.postProcessors
                configProperties = bundle.configProperties
                sourcesDir = tmpRoot
                outputDir = dstRoot

                mustRunAfter prepareAssets
            }
            processWebResources.dependsOn compileWeb
        }

        processWebResources.with {
            from (new File(tmpRoot, webResources.staticFolder)) {
                include "**"
            }
            into dstRoot
        }
        if (webResources.assets != null) {
            processWebResources.with webResources.assets.from(tmpRoot)
        }

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
