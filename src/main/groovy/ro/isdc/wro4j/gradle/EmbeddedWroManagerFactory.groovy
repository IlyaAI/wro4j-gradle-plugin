package ro.isdc.wro4j.gradle

import ro.isdc.wro.WroRuntimeException
import ro.isdc.wro.extensions.model.factory.GroovyModelFactory
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory
import ro.isdc.wro.model.factory.WroModelFactory
import ro.isdc.wro.model.factory.XmlModelFactory

class EmbeddedWroManagerFactory extends ConfigurableWroManagerFactory {
    private final File wroFile
    private final Properties configProperties

    EmbeddedWroManagerFactory(File wroFile, Properties configProperties) {
        this.wroFile = wroFile
        this.configProperties = configProperties
    }

    @Override
    protected Properties newConfigProperties() {
        return configProperties
    }

    @Override
    protected WroModelFactory newModelFactory() {
        if (wroFile.name.endsWith(".xml")) {
            return new XmlModelFactory() {
                @Override
                protected InputStream getModelResourceAsStream() throws IOException {
                    return new FileInputStream(wroFile)
                }
            }
        }
        if (wroFile.name.endsWith(".groovy")) {
            return new GroovyModelFactory() {
                @Override
                protected InputStream getModelResourceAsStream() throws IOException {
                    return new FileInputStream(wroFile)
                }
            }
        }

        throw new WroRuntimeException(
            String.format("Unknown WroModel format '{}'. Hint: use either .xml or .groovy", wroFile)
        )
    }
}
