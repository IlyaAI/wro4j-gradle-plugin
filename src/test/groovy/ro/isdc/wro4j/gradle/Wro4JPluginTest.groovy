package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.internal.AbstractTask
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class Wro4JPluginTest extends ProjectSpec {

    def setup() {
        project.apply plugin: "java"
        project.apply plugin: "wro4j"
    }

    def processWebResources() {
        project.tasks.getByPath("prepareAssets").execute()

        def processWebResources = project.tasks.getByPath("processWebResources")
        processWebResources.dependsOn.each { task ->
            if ((task instanceof Task) && task.name != "prepareAssets") {
                task.execute()
            }
        }
        processWebResources.execute()
    }

    def "should process web resources"() {
        given:
        WebResourceSet.get(project).with {
            srcDir = new File(getClass().getResource("/root").toURI()).parentFile

            bundle("core") {
                js "js/**.js"
                preProcessor "jsMin"
            }

            bundle("libs") {
                js "libs/*.min.js"
            }

            bundle("theme-default") {
                css "css/default/*.css"

                preProcessor "cssUrlRewriting", "cssMin"
            }

            assets {
                include "css/default/images/*.png"
            }
        }

        when:
        project.evaluate()
        processWebResources()

        then:
        def jsCore = new File(project.buildDir, "resources/main/static/core.js")
        jsCore.exists()
        def coreText = jsCore.getText()
        coreText.contains("function api(){}")
        coreText.contains("function main(){}")

        def jsLibs = new File(project.buildDir, "resources/main/static/libs.js")
        jsLibs.exists()
        def libsText = jsLibs.getText()
        libsText.contains("function libA ( ) { }")
        libsText.contains("function libB ( ) { }")

        def cssDefault = new File(project.buildDir, "resources/main/static/theme-default.css")
        cssDefault.exists()
        def defaultText = cssDefault.getText()
        defaultText.contains(".logo{")
        defaultText.contains(".helper{color:gray;}")
        defaultText.contains("url(\"./css/default/images/logo.png\"")

        def logoPng = new File(project.buildDir, "resources/main/static/css/default/images/logo.png")
        logoPng.exists()

        def junkTxt = new File(project.buildDir, "resources/main/static/css/default/images/junk.txt")
        !junkTxt.exists()

        def jsDir = new File(project.buildDir, "resources/main/static/js")
        !jsDir.exists()

        def libsDir = new File(project.buildDir, "resources/main/static/libs")
        !libsDir.exists()

        def indexHtml = new File(project.buildDir, "resources/main/static/index.html")
        indexHtml.exists()
    }

    def "should resolve webjars"() {
        given:
        def webResources = WebResourceSet.get(project)
        webResources.with {
            srcDir = new File(getClass().getResource("/root").toURI()).parentFile

            bundle("libs") {
                js "webjars/mywebjar/1.0.0/my.webjar.js"
            }

            bundle("theme-default") {
                css "webjars/mywebjar/1.0.0/my.webjar.css"

                preProcessor "cssUrlRewriting"
            }
        }
        project.dependencies.add("webjars", project.files("${webResources.srcDir}/my-webjar.jar"))

        when:
        project.evaluate()
        processWebResources()

        then:
        def jsLibs = new File(project.buildDir, "resources/main/static/libs.js")
        jsLibs.exists()
        def libsText = jsLibs.getText()
        libsText.contains("function myWebJar() {}")

        def cssDefault = new File(project.buildDir, "resources/main/static/theme-default.css")
        cssDefault.exists()
        def defaultText = cssDefault.getText()
        defaultText.contains("url(\"./webjars/mywebjar/1.0.0/images/logo.png\"")

        def jsDir = new File(project.buildDir, "resources/main/static/js")
        !jsDir.exists()
    }

    def "should override css import"() {
        given:
        def webResources = WebResourceSet.get(project)
        webResources.with {
            srcDir = new File(getClass().getResource("/root").toURI()).parentFile

            bundle("theme-default") {
                css "webjars/mywebjar/1.0.0/my.webjar.css"

                cssOverrideImport "colors.css", "../../../css/default/my.colors.css"
                preProcessor "cssUrlRewriting"
            }
        }
        project.dependencies.add("webjars", project.files("${webResources.srcDir}/my-webjar.jar"))

        when:
        project.evaluate()
        processWebResources()

        then:
        def cssDefault = new File(project.buildDir, "resources/main/static/theme-default.css")
        cssDefault.exists()
        def defaultText = cssDefault.getText()
        defaultText.contains(".title { color: white; }")
        defaultText.contains(".text { color: lightgray; }")
    }
}
