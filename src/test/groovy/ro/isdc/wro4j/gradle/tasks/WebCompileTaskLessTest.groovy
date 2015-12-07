package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec

class WebCompileTaskLessTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        project.apply plugin: 'wro4j'

        task = project.tasks.create('compileLess', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-3.xml").toURI())
        task.preProcessor("less4j")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir
    }

    def "should compile less to css"() {
        given:
        task.targetGroup("less")

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "less.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("color: #eee;")
        text.contains("background-color: #fff;")
    }
}
