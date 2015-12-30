package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskBasicTest extends ProjectSpec {

    WebBundle jsBundle;
    WebBundle cssBundle;
    WebCompileTask task

    def setup() {
        jsBundle = new WebBundle(project, "js-test")
        jsBundle.js("/test-1.js", "/test-2.js")

        cssBundle = new WebBundle(project, "css-test")
        cssBundle.css("/test-1.css", "/test-2.css")

        task = project.tasks.create('compileWebTest', WebCompileTask)
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should bundle js"() {
        given:
        task.bundle = jsBundle

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "js-test.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1(arg10, arg11) {")
        text.contains("function test2(arg20, arg21) {")
    }

    def "should apply js pre-processors"() {
        given:
        jsBundle.preProcessor("jsMin")
        task.bundle = jsBundle

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "js-test.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1(arg10,arg11){")
        text.contains("function test2(arg20,arg21){")
    }

    def "should apply js post-processors"() {
        given:
        jsBundle.postProcessor("jsMin")
        task.bundle = jsBundle

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "js-test.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1(arg10,arg11){")
        text.contains("function test2(arg20,arg21){")
    }

    def "should bundle css"() {
        given:
        task.bundle = cssBundle

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "css-test.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains(".test1  {")
        text.contains(".test2  {")
    }

    def "should apply css pre-processors"() {
        given:
        cssBundle.preProcessor("cssMin")
        task.bundle = cssBundle

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "css-test.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("border:solid 1px black;")
        text.contains("border:dashed 1px black;")
    }

    def "should apply css post-processors"() {
        given:
        cssBundle.postProcessor("cssMin")
        task.bundle = cssBundle

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "css-test.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("border:solid 1px black;")
        text.contains("border:dashed 1px black;")
    }
}
