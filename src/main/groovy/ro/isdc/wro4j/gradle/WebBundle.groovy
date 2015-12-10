package ro.isdc.wro4j.gradle

import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator

class WebBundle {
    private final List<String> preProcessors = []
    private final List<String> postProcessors = []
    private final Group group

    WebBundle(String name) {
        group = new Group(name)
    }

    String getName() {
        return group.getName()
    }

    Group getGroup() {
        return group
    }

    List<String> getPreProcessors() {
        return Collections.unmodifiableList(preProcessors)
    }

    void preProcessor(String... pre) {
        preProcessors.addAll(pre)
    }

    List<String> getPostProcessors() {
        return Collections.unmodifiableList(postProcessors)
    }

    void postProcessor(String... post) {
        postProcessors.addAll(post)
    }

    void js(String... resources) {
        resources.each { resource ->
            group.addResource(Resource.create(uriOf(resource), ResourceType.JS))
        }
    }

    void css(String... resources) {
        resources.each { resource ->
            group.addResource(Resource.create(uriOf(resource), ResourceType.CSS))
        }
    }

    void css(String resource, Closure config) {
        group.addResource(Resource.create(uriOf(resource), ResourceType.CSS))

        // TODO: not finished
        def spec = new CssSpec()
        config.delegate = spec
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config()
    }

    private static String uriOf(String resource) {
        if (resource.startsWith(ServletContextUriLocator.PREFIX)) {
            return resource
        }

        return ServletContextUriLocator.PREFIX + resource
    }
}
