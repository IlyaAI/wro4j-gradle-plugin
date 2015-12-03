package ro.isdc.wro4j.gradle.tasks

import nebula.test.ProjectSpec
import ro.isdc.wro.manager.factory.WroManagerFactory
import ro.isdc.wro4j.gradle.Wro4JSettings

class Wro4JSettingsTest extends ProjectSpec {

    def "should be available after plugin apply"() {
        when:
        project.apply plugin: 'wro4j'
        def settings = Wro4JSettings.get(project)

        then:
        settings != null
        settings.wroManagerFactory != null
    }

    def "should copy all properties"() {
        given:
        def origin = new Wro4JSettings(project)
        origin.wroManagerFactory = WroManagerFactory
        origin.targetGroups = ["origin"]

        when:
        def copy = origin.copy()

        then:
        copy != null
        copy.wroManagerFactory == WroManagerFactory
        copy.targetGroups.contains("origin")
    }

    def "should copy targetGroups deeply"() {
        given:
        def origin = new Wro4JSettings(project)
        origin.targetGroups = ["origin"]

        when:
        def copy = origin.copy()
        copy.targetGroups += "copy"

        then:
        copy != null
        copy.targetGroups.size() == 2
        copy.targetGroups.contains("origin")
        copy.targetGroups.contains("copy")
        origin.targetGroups.size() == 1
    }
}
