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

        def cssGroup = new Group("url-rewrite-1")
        cssGroup.addResource(Resource.create("/a/test-3.css", ResourceType.CSS))
        cssGroup.addResource(Resource.create("/b/test-4.css", ResourceType.CSS))
        wroModel.addGroup(cssGroup)

        cssGroup = new Group("url-rewrite-2")
        cssGroup.addResource(Resource.create("/c/d/test-8.css", ResourceType.CSS))
        wroModel.addGroup(cssGroup)

        task = project.tasks.create('compileCssTest', WebCompileTask)
        task.wroModel = wroModel
        task.preProcessors = ["cssUrlRewriting"]
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should change image url"() {
        given:
        task.targetGroups = ["url-rewrite-1"]
        task.postProcessors = ["cssUrlUnroot"]

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
        task.targetGroups = ["url-rewrite-2"]
        task.preProcessors = ["cssImport", "cssUrlRewriting"]
        task.postProcessors = ["cssUrlUnroot"]

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
