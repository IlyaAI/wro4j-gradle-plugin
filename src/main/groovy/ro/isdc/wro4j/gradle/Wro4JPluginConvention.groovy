package ro.isdc.wro4j.gradle

import groovy.lang.Closure

class Wro4JPluginConvention {

    def wro4j(Closure closure) {
        closure.delegate = this
        closure()
    }
}
