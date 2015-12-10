package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec

class WebCompileTaskWildcardsTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        project.apply plugin: 'java'
        project.apply plugin: 'wro4j'

        task = project.tasks.create('compileWebTest', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-4.xml").toURI())
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir
    }

    def "should support simple wildcards in servletContext uri"() {
        given:
        task.targetGroup("simple-wildcards-context")

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "simple-wildcards-context.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1")
        text.contains("function test2")
        !text.contains("function test3")
        !text.contains("function test4")
    }

    def "should support deep wildcards in servletContext uri"() {
        given:
        task.targetGroup("deep-wildcards-context")

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "deep-wildcards-context.js")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("function test1")
        text.contains("function test2")
        text.contains("function test3")
        text.contains("function test4")
    }
}
