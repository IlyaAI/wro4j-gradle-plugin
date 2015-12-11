package ro.isdc.wro4j.gradle

import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator
import ro.isdc.wro4j.extensions.CssImportOverridePreProcessor

class WebBundle {
    private final List<String> preProcessors = []
    private final List<String> postProcessors = []
    private Map<String, String> configProperties = new HashMap<>()
    private final Group group
    private boolean hasJs = false
    private boolean hasCss = false

    WebBundle(String name) {
        group = new Group(name)
    }

    String getName() {
        return group.getName()
    }

    Group getGroup() {
        return group
    }

    boolean getHasJs() {
        return hasJs
    }

    boolean getHasCss() {
        return hasCss
    }

    List<String> getPreProcessors() {
        return Collections.unmodifiableList(preProcessors)
    }

    /**
     * Defines pre-processor(s) to be applied to resources in this bundle.
     *
     * @param pre    pre-processor name
     */
    void preProcessor(String... pre) {
        preProcessors.addAll(pre)
    }

    List<String> getPostProcessors() {
        return Collections.unmodifiableList(postProcessors)
    }

    /**
     * Defines post-processor(s) to be applied to resources in this bundle.
     *
     * @param post    post-processor name
     */
    void postProcessor(String... post) {
        postProcessors.addAll(post)
    }

    Map<String, String> getConfigProperties() {
        return Collections.unmodifiableMap(configProperties)
    }

    /**
     * Defines JavaScript resources.
     *
     * @param resources    resource uri-s against {@link WebResourceSet#srcMainDir}
     */
    void js(String... resources) {
        resources.each { resource ->
            group.addResource(Resource.create(uriOf(resource), ResourceType.JS))
        }
        hasJs = true
    }

    /**
     * Defines Cascade Style Sheet resources.
     *
     * @param resources    resource uri-s against {@link WebResourceSet#srcMainDir}
     */
    void css(String... resources) {
        resources.each { resource ->
            group.addResource(Resource.create(uriOf(resource), ResourceType.CSS))
        }
        hasCss = true
    }

    /**
     * Defines special pre-processor to override specified import with given one.
     * E.g. this useful to provide custom variables.less for Twitter Bootstrap taken from webjar.
     *
     * @param resources    resource uri-s against {@link WebResourceSet#srcMainDir}
     */
    void cssOverrideImport(String from, String with) {
        preProcessor(CssImportOverridePreProcessor.ALIAS)
        configProperties.put(CssImportOverridePreProcessor.encodeKey(from), with)
    }

    private static String uriOf(String resource) {
        if (resource.startsWith(ServletContextUriLocator.PREFIX)) {
            return resource
        }

        return ServletContextUriLocator.PREFIX + resource
    }
}
