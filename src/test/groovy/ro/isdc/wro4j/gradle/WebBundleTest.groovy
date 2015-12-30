package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec

class WebBundleTest extends ProjectSpec {
    File sourcesDir

    def setup() {
        sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
    }

    def "should preserve file order"() {
        given:
        def bundle = new WebBundle(project, "test")
        bundle.js("test-1.js", "test-2.js")
        bundle.css("test-2.css", "test-1.css")

        when:
        def paths = bundle.getAllPaths(sourcesDir)

        then:
        paths.getAt(0).pathString == "test-1.js"
        paths.getAt(1).pathString == "test-2.js"
        paths.getAt(2).pathString == "test-2.css"
        paths.getAt(3).pathString == "test-1.css"
    }

    def "should preserve file order (inverted)"() {
        given:
        def bundle = new WebBundle(project, "test")
        bundle.js("test-2.js", "test-1.js")
        bundle.css("test-1.css", "test-2.css")

        when:
        def paths = bundle.getAllPaths(sourcesDir)

        then:
        paths.getAt(0).pathString == "test-2.js"
        paths.getAt(1).pathString == "test-1.js"
        paths.getAt(2).pathString == "test-1.css"
        paths.getAt(3).pathString == "test-2.css"
    }

    def "should preserve file order (wildcards)"() {
        given:
        def bundle = new WebBundle(project, "test")
        bundle.js("test-2.js", "*.js")
        bundle.css("test-1.css", "*.css")

        when:
        def paths = bundle.getAllPaths(sourcesDir)

        then:
        paths.getAt(0).pathString == "test-2.js"
        paths.getAt(1).pathString == "test-1.js"
        paths.getAt(2).pathString == "test-1.css"
        paths.getAt(3).pathString == "test-2.css"
    }

    def "should preserve file order (wildcards,inverted)"() {
        given:
        def bundle = new WebBundle(project, "test")
        bundle.js("test-1.js", "*.js")
        bundle.css("test-2.css", "*.css")

        when:
        def paths = bundle.getAllPaths(sourcesDir)

        then:
        paths.getAt(0).pathString == "test-1.js"
        paths.getAt(1).pathString == "test-2.js"
        paths.getAt(2).pathString == "test-2.css"
        paths.getAt(3).pathString == "test-1.css"
    }
}
