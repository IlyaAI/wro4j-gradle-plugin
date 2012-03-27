package ro.isdc.wro4j.gradle.tasks.tools

import ro.isdc.wro.extensions.model.factory.WroModelDelegate
import ro.isdc.wro.model.WroModel

class WroModelFromClosure {

    static WroModel read(Closure closure){
        WroModelDelegate delegate = new WroModelDelegate()
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = delegate
        delegate.wroModel
    }
}
