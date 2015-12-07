package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec

class WebCompileTaskBasicTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        project.apply plugin: 'wro4j'

        task = project.tasks.create('compileWeb', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir
    }

    def "should support classpath uri"() {
        given:
        task.targetGroup("test-classpath")

        when:
        project.evaluate()
        task.execute()

        then:
        def jsFile = new File(project.buildDir, "test-classpath.js")
        jsFile.exists()
        jsFile.length() > 0
        def cssFile = new File(project.buildDir, "test-classpath.css")
        cssFile.exists()
        cssFile.length() > 0
    }

    def "should support servletContext uri"() {
        given:
        task.targetGroup("test-context")

        when:
        project.evaluate()
        task.execute()

        then:
        def jsFile = new File(project.buildDir, "test-context.js")
        jsFile.exists()
        jsFile.length() > 0
        def cssFile = new File(project.buildDir, "test-context.css")
        cssFile.exists()
        cssFile.length() > 0
    }

    def "should bundle js"() {
        given:
        task.targetGroup("js-test")

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
        task.targetGroup("js-test")
        task.preProcessor("jsMin")

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
        task.targetGroup("js-test")
        task.postProcessor("jsMin")

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
        task.targetGroup("css-test")

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
        task.targetGroup("css-test")
        task.preProcessor("cssMin")

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
        task.targetGroup("css-test")
        task.postProcessor("cssMin")

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
