package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskUrlRewriteTest extends ProjectSpec {

    WebBundle cssBundleA
    WebBundle cssBundleB
    WebCompileTask task

    def setup() {
        cssBundleA = new WebBundle(project, "url-rewrite-1")
        cssBundleA.css("/a/test-3.css", "/b/test-4.css")
        cssBundleA.cssRewriteUrl()

        cssBundleB = new WebBundle(project, "url-rewrite-2")
        cssBundleB.css("/c/d/test-8.css")
        cssBundleA.cssRewriteUrl()

        task = project.tasks.create('compileCssTest', WebCompileTask)
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should change image url"() {
        given:
        task.bundle = cssBundleA

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "url-rewrite-1.css")
        file.exists()
        def text = file.getText()
        text.contains("a/images/logoA.png")
        text.contains("b/images/logoB.png")
    }

    def "should change deep image url"() {
        given:
        cssBundleB.preProcessor("cssImport")
        cssBundleB.cssRewriteUrl()
        task.bundle = cssBundleB

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "url-rewrite-2.css")
        file.exists()
        def text = file.getText()
        text.contains("c/e/image.png")
    }
}
