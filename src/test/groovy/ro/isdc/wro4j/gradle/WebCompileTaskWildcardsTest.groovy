package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskWildcardsTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        def wroModel = new WroModel()

        def jsSimple = new Group("simple-wildcards")
        jsSimple.addResource(Resource.create("/*.js", ResourceType.JS))
        wroModel.addGroup(jsSimple)

        def jsDeep = new Group("deep-wildcards")
        jsDeep.addResource(Resource.create("/**.js", ResourceType.JS))
        wroModel.addGroup(jsDeep)

        task = project.tasks.create('compileWebTest', WebCompileTask)
        task.wroModel = wroModel
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should support simple wildcards"() {
        given:
        task.targetGroups = ["simple-wildcards"]

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "simple-wildcards.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1")
        text.contains("function test2")
        !text.contains("function test3")
        !text.contains("function test4")
    }

    def "should support deep wildcards"() {
        given:
        task.targetGroups = ["deep-wildcards"]

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "deep-wildcards.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1")
        text.contains("function test2")
        text.contains("function test3")
        text.contains("function test4")
    }
}
