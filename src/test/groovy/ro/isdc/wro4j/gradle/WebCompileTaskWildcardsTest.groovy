package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskWildcardsTest extends ProjectSpec {

    WebBundle jsSimple;
    WebBundle jsDeep;
    WebCompileTask task

    def setup() {
        jsSimple = new WebBundle(project, "simple-wildcards")
        jsSimple.js("/*.js")

        jsDeep = new WebBundle(project, "deep-wildcards")
        jsDeep.js("/**/*.js")

        task = project.tasks.create('compileWebTest', WebCompileTask)
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should support simple wildcards"() {
        given:
        task.bundle = jsSimple

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
        task.bundle = jsDeep

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
