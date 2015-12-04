package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec

class WebCompileTaskTest extends ProjectSpec {

    def setup() {
        project.apply plugin: 'wro4j'
    }

    /*def "should have default settings after creation"() {
        given:
        def settings = new Wro4JSettings(project)

        when:
        def task = project.tasks.create('test', CompileWebTask)
        this.project.evaluate()

        then:
        task.wroManagerFactory == settings.wroManagerFactory
        task.wroFile == settings.wroFile
    }*/

    def "should bundle js"() {
        given:
        def task = project.tasks.create('compileJs', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.targetGroup("js-test")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir

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
        def task = project.tasks.create('compileJs', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.targetGroup("js-test")
        task.preProcess("jsMin")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir

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
        def task = project.tasks.create('compileJs', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.targetGroup("js-test")
        task.postProcess("jsMin")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir

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
        def task = project.tasks.create('compileCss', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.targetGroup("css-test")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir

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
        def task = project.tasks.create('compileCss', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.targetGroup("css-test")
        task.preProcess("cssMin")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir

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
        def task = project.tasks.create('compileCss', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-1.xml").toURI())
        task.targetGroup("css-test")
        task.postProcess("cssMin")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir

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
