package ro.isdc.wro4j.gradle

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class Wro4JPlugin implements Plugin<Project> {
    private Wro4JSettings settings;

    @Override
    public void apply(Project project) {
        settings = project.extensions.create(Wro4JSettings.NAME, Wro4JSettings, project)
    }
}
