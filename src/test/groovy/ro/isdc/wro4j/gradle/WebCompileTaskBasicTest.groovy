package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskBasicTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        def wroModel = new WroModel()

        def jsGroup = new Group("js-test")
        jsGroup.addResource(Resource.create("/test-1.js", ResourceType.JS))
        jsGroup.addResource(Resource.create("/test-2.js", ResourceType.JS))
        wroModel.addGroup(jsGroup)

        def cssGroup = new Group("css-test")
        cssGroup.addResource(Resource.create("/test-1.css", ResourceType.CSS))
        cssGroup.addResource(Resource.create("/test-2.css", ResourceType.CSS))
        wroModel.addGroup(cssGroup)

        task = project.tasks.create('compileWebTest', WebCompileTask)
        task.wroModel = wroModel
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should bundle js"() {
        given:
        task.targetGroups = ["js-test"]

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
        task.targetGroups = ["js-test"]
        task.preProcessors = ["jsMin"]

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
        task.targetGroups = ["js-test"]
        task.postProcessors = ["jsMin"]

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
        task.targetGroups = ["css-test"]

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
        task.targetGroups = ["css-test"]
        task.preProcessors = ["cssMin"]

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
        task.targetGroups = ["css-test"]
        task.postProcessors = ["cssMin"]

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
