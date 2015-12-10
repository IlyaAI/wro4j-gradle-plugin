package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskLessTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        def wroModel = new WroModel()

        def cssGroup = new Group("less")
        cssGroup.addResource(Resource.create("/test-5.less", ResourceType.CSS))
        wroModel.addGroup(cssGroup)

        task = project.tasks.create('compileLessTest', WebCompileTask)
        task.wroModel = wroModel
        task.preProcessors = ["less4j"]
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should compile less to css"() {
        given:
        task.targetGroups = ["less"]

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
