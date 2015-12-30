package ro.isdc.wro4j.gradle

import nebula.test.ProjectSpec

class WebCompileTaskInOutTest extends ProjectSpec {

    WebCompileTask task

    def setup() {
        task = project.tasks.create('compileTest', WebCompileTask)
        task.sourcesDir = new File(getClass().getResource("/root").toURI()).parentFile
        task.outputDir = project.buildDir
    }

    def "should return sourceFiles per-file level (js)"() {
        given:
        def bundle = new WebBundle(project, "test")
        bundle.js("test-1.js")
        task.bundle = bundle

        when:
        def srcFiles = task.sourceFiles

        then:
        srcFiles.size() == 1
        srcFiles.contains(new File(task.sourcesDir, "test-1.js"))
    }

    def "should return sourceFiles per-file level (css)"() {
        given:
        def bundle = new WebBundle(project, "test")
        bundle.css("d/main.css")
        bundle.cssOverrideImport("variables.css", "my-variables.css")
        task.bundle = bundle

        when:
        def srcFiles = task.sourceFiles

        then:
        srcFiles.size() == 4
        srcFiles.contains(new File(task.sourcesDir, "d/main.css"))
        srcFiles.contains(new File(task.sourcesDir, "d/my-variables.css"))
        srcFiles.contains(new File(task.sourcesDir, "d/nested-1.css"))
        srcFiles.contains(new File(task.sourcesDir, "d/nested-2.css"))
    }

    def "should return outputFiles per-file level (js)"() {
        given:
        def bundle = new WebBundle(project, "my-test")
        bundle.js("test-1.js")
        task.bundle = bundle

        when:
        def outFiles = task.outputFiles

        then:
        outFiles.size() == 1
        outFiles.contains(new File(task.outputDir, "my-test.js"))
    }

    def "should return outputFiles per-file level (css)"() {
        given:
        def bundle = new WebBundle(project, "my-test")
        bundle.css("test-1.css")
        task.bundle = bundle

        when:
        def outFiles = task.outputFiles

        then:
        outFiles.size() == 1
        outFiles.contains(new File(task.outputDir, "my-test.css"))
    }

    def "should return outputFiles per-file level (js+css)"() {
        given:
        def bundle = new WebBundle(project, "my-test")
        bundle.js("test-1.js")
        bundle.css("test-1.css")
        task.bundle = bundle

        when:
        def outFiles = task.outputFiles

        then:
        outFiles.size() == 2
        outFiles.contains(new File(task.outputDir, "my-test.js"))
        outFiles.contains(new File(task.outputDir, "my-test.css"))
    }
}
