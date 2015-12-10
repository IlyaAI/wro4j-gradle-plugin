package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import org.gradle.api.DefaultTask
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

    def "should process web resources"() {
        given:
        WebResourceSet.get(project).with {
            srcDir = new File(getClass().getResource("/root").toURI()).parentFile

            bundle ("core") {
                js "js/**.js"
                preProcessor "jsMin"
            }

            bundle ("libs") {
                js "libs/*.min.js"
            }

            bundle ("theme-default") {
                css "css/default/*.css"

                preProcessor "cssUrlRewriting", "cssMin"
            }

            assets {
                include "css/default/images/*.png"
            }
        }

        when:
        project.evaluate()
        project.tasks.getByPath("processWebResources").dependsOn.each { task ->
            if (task instanceof AbstractTask) {
                task.execute()
            }
        }

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
}
