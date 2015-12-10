package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType

class WebCompileTaskUrlRewriteTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        def wroModel = new WroModel()

        def cssGroup = new Group("url-rewrite")
        cssGroup.addResource(Resource.create("/a/test-3.css", ResourceType.CSS))
        cssGroup.addResource(Resource.create("/b/test-4.css", ResourceType.CSS))
        wroModel.addGroup(cssGroup)

        task = project.tasks.create('compileCssTest', WebCompileTask)
        task.wroModel = wroModel
        task.preProcessors = ["cssUrlRewriting"]
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should change image url"() {
        given:
        task.targetGroups = ["url-rewrite"]

        when:
        project.evaluate()
        task.execute()

        then:
        def file = new File(project.buildDir, "url-rewrite.css")
        file.exists()
        def text = file.getText("utf-8")
        text.contains("./a/images/logoA.png")
        text.contains("./b/images/logoB.png")
    }
}
