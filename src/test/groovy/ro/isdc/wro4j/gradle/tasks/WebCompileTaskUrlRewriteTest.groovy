package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec

class WebCompileTaskUrlRewriteTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        project.apply plugin: 'wro4j'

        task = project.tasks.create('compileCss', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-2.xml").toURI())
        task.targetGroup("url-rewrite")
        task.preProcessor("cssUrlRewriting")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir
    }

    def "should change image url"() {
        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "url-rewrite.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("a/images/logoA.png")
        text.contains("b/images/logoB.png")
    }
}
