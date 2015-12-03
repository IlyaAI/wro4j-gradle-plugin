package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec
import ro.isdc.wro4j.gradle.Wro4JSettings

class OptimizeResourcesTaskTest extends ProjectSpec {

    def setup() {
        project.apply plugin: 'wro4j'
    }

    def "should have default settings after creation"() {
        given:
        def settings = new Wro4JSettings(project)

        when:
        def task = project.tasks.create('test', OptimizeResourcesTask)
        this.project.evaluate()

        then:
        task.wroManagerFactory == settings.wroManagerFactory
        task.wroFile == settings.wroFile
    }
}
