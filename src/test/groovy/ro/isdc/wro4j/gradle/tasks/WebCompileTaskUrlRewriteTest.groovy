package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec

class WebCompileTaskUrlRewriteTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        project.apply plugin: 'java'
        project.apply plugin: 'wro4j'

        task = project.tasks.create('compileCssTest', WebCompileTask)
        task.wroFile = new File(getClass().getResource("/wro-2.xml").toURI())
        task.preProcessor("cssUrlRewriting")
        task.sourcesDir = task.wroFile.parentFile
        task.outputDir = project.buildDir
    }

    def "should change image url for classpath uri"() {
        given:
        task.targetGroup("url-rewrite-classpath")

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "url-rewrite-classpath.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("classpath:a/images/logoA.png")
        text.contains("classpath:b/images/logoB.png")
    }

    def "should change image url for servletContext uri"() {
        given:
        task.targetGroup("url-rewrite-context")

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "url-rewrite-context.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("./a/images/logoA.png")
        text.contains("./b/images/logoB.png")
    }
}
