package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskLessTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        def cssBundle = new WebBundle(project, "less")
        cssBundle.css("/test-5.less")
        cssBundle.preProcessor("less4j")

        task = project.tasks.create('compileLessTest', WebCompileTask)
        task.bundle = cssBundle
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should compile less to css"() {
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
