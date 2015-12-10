package ro.isdc.wro4j.gradle

import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.factory.WroModelFactory

class EmbeddedWroManagerFactory extends ConfigurableWroManagerFactory {
    private final WroModel wroModel
    private final Properties configProperties

    EmbeddedWroManagerFactory(WroModel wroModel, Properties configProperties) {
        this.wroModel = wroModel
        this.configProperties = configProperties
    }

    @Override
    protected Properties newConfigProperties() {
        return configProperties
    }

    @Override
    protected WroModelFactory newModelFactory() {
        return new WroModelFactory() {
            @Override
            void destroy() {
            }

            @Override
            WroModel create() {
                return wroModel
            }
        }
    }
}
