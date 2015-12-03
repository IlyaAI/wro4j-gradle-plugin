package ro.isdc.wro4j.gradle

import org.gradle.api.Project
import ro.isdc.wro.extensions.manager.standalone.GoogleStandaloneManagerFactory
import ro.isdc.wro.manager.factory.WroManagerFactory

class Wro4JSettings implements Cloneable {
    public final static String NAME = 'wro4j'

    def Class<? extends WroManagerFactory> wroManagerFactory
    def File wroFile;
    def Set<String> targetGroups = []
    def File contextFolder
    def File destinationFolder
    def File jsDestinationFolder
    def File cssDestinationFolder

    Wro4JSettings(Project project) {
        wroManagerFactory = GoogleStandaloneManagerFactory
        wroFile = new File(project.rootDir, 'wro.xml')
        targetGroups += 'all'
        contextFolder = project.rootDir
        destinationFolder = project.buildDir
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        def copy = (Wro4JSettings) super.clone()
        copy.targetGroups = (Set)this.targetGroups.clone()
        return copy
    }

    Wro4JSettings copy() {
        try {
            return (Wro4JSettings) clone()
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage(), e) // this is the thing that should not be
        }
    }

    static Wro4JSettings get(Project project) {
        return project.extensions.getByType(Wro4JSettings)
    }
}
