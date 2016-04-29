package ro.isdc.wro4j.gradle

import groovy.transform.PackageScope
import org.apache.commons.lang.StringUtils
import org.gradle.api.Project
import org.gradle.api.file.RelativePath
import org.gradle.api.tasks.Input
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor
import ro.isdc.wro4j.extensions.CssImportOverridePreProcessor
import ro.isdc.wro4j.extensions.CssUrlUnrootPostProcessor

class WebBundle {
    private final List<String> preProcessors = []
    private final List<String> postProcessors = []
    private final List<String> css = []
    private final List<String> js = []
    private final Project project
    private final String name
    private Map<String, String> configProperties = new HashMap<>()
    private boolean gzipped;

    WebBundle(Project project, String name) {
        this.project = project
        this.name = name
    }

    String getName() {
        return name
    }

    boolean getHasJs() {
        return !js.isEmpty()
    }

    boolean getHasCss() {
        return !css.isEmpty()
    }

    @Input
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

    @Input
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

    @Input
    Map<String, String> getConfigProperties() {
        return Collections.unmodifiableMap(configProperties)
    }

    /**
     * If set to true then both compressed and non-compressed versions of bundles are produced.
     * Compressed one will have ".gz" suffix in its name.
     *
     * @return
     */
    @Input
    boolean isGzipped() {
        return gzipped;
    }

    void enableGzip() {
        gzipped = true;
    }

    /**
     * Defines JavaScript resources.
     *
     * @param resources    resource uri-s against {@link WebResourceSet#srcMainDir}
     */
    void js(String... resources) {
        js.addAll(resources)
    }

    /**
     * Defines Cascade Style Sheet resources.
     *
     * @param resources    resource uri-s against {@link WebResourceSet#srcMainDir}
     */
    void css(String... resources) {
        css.addAll(resources)
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

    /**
     * Defines 'cssUrlRewriting' pre-processor and special internal
     * post-processor 'cssUrlUnroot' to trim beginning slash.
     */
    void cssRewriteUrl() {
        preProcessor(CssUrlRewritingProcessor.ALIAS)
        postProcessor(CssUrlUnrootPostProcessor.ALIAS)
    }

    @PackageScope
    Set<RelativePath> getCssPaths(File baseDir) {
        def resolvedPaths = new LinkedHashSet<RelativePath>()
        resolve(baseDir, css, resolvedPaths)
        return resolvedPaths
    }

    @PackageScope
    Set<RelativePath> getJsPaths(File baseDir) {
        def resolvedPaths = new LinkedHashSet<RelativePath>()
        resolve(baseDir, js, resolvedPaths)
        return resolvedPaths
    }

    @PackageScope
    Set<RelativePath> getAllPaths(File baseDir) {
        def resolvedPaths = new LinkedHashSet<RelativePath>()
        resolve(baseDir, js, resolvedPaths)
        resolve(baseDir, css, resolvedPaths)
        return resolvedPaths
    }

    private void resolve(File baseDir, Iterable<String> srcPaths, Set<RelativePath> resolvedPaths) {
        for (def srcPath: srcPaths) {
            project
                .fileTree(dir: baseDir, include: StringUtils.removeStart(srcPath, "/"))
                .visit { details ->
                    if (!details.isDirectory()) {
                        resolvedPaths.add(details.relativePath)
                    }
                }
        }
    }
}
