package ro.isdc.wro4j.gradle

import org.gradle.api.Project

class WebResourceSet {
    static final String NAME = "webResources"

    private File srcDir
    private Set<WroSpec> jsSpecs = []
    private Set<WroSpec> themeSpecs = []

    WebResourceSet(Project project) {
        srcDir = project.file("src/main/webResources")
    }

    File getSrcDir() {
        return srcDir
    }

    void setSrcDir(File srcDir) {
        this.srcDir = srcDir
    }

    Set<WroSpec> getJsSpecs() {
        return jsSpecs
    }

    void js(String name) {
        js([name])
    }

    void js(Iterable<String> names) {
        js(names, {})
    }

    void js(String name, Closure config) {
        js([name], config)
    }

    void js(Iterable<String> names, Closure config) {
        names.each {name ->
            def spec = new WroSpec(name);
            config.delegate = spec
            config.resolveStrategy = Closure.DELEGATE_ONLY
            config()

            jsSpecs.add(spec)
        }
    }

    Set<WroSpec> getThemeSpecs() {
        return themeSpecs
    }

    void theme(String name) {
        theme([name])
    }

    void theme(Iterable<String> names) {
        theme(names, {})
    }

    void theme(String name, Closure config) {
        theme([name], config)
    }

    void theme(Iterable<String> names, Closure config) {
        names.each {name ->
            def spec = new WroSpec(name);
            spec.preProcessor("cssUrlRewriting")

            config.delegate = spec
            config.resolveStrategy = Closure.DELEGATE_ONLY
            config()

            themeSpecs.add(spec)
        }
    }
}
