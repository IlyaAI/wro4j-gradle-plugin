package ro.isdc.wro4j.gradle

import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import ro.isdc.wro.model.WroModel

class WebResourceSet {
    private static final String STATIC = "static"
    static final String NAME = "webResources"

    private final Project project
    private final Map<String, WebBundle> bundles = new HashMap<>()
    private CopySpec assets;
    private File srcDir
    private String staticFolder

    WebResourceSet(Project project) {
        this.project = project
        this.srcDir = project.file("src/main/webResources")
        this.staticFolder = STATIC
    }

    File getSrcDir() {
        return srcDir
    }

    void setSrcDir(File srcDir) {
        this.srcDir = srcDir
    }

    /**
     * Folder name for webapp static content. Default 'static'.
     *
     * @return folder for static content
     */
    String getStaticFolder() {
        return staticFolder
    }

    void setStaticFolder(String staticFolder) {
        this.staticFolder = staticFolder
    }

    Collection<WebBundle> getBundles() {
        return Collections.unmodifiableCollection(bundles.values())
    }

    /**
     * Defines web resource bundle. Resources from bundle will be pre- and post-processed by specified set of processors.
     *
     * @param name  bundle name; generated output files will be <name>.js and/or <name>.css
     * @param configure    closure to configure bundle
     * @see WebBundle
     */
    void bundle(String name, Closure configure) {
        def bundle = bundles.get(name)
        if (bundle == null) {
            bundle = new WebBundle(name)
            bundles.put(name, bundle)
        }

        configure.delegate = bundle
        configure.resolveStrategy = Closure.DELEGATE_FIRST
        configure()
    }

    /**
     * Defines web assets. Assets will be copied to static folder as is.
     *
     * @param configure    closure to configure assets
     * @see CopySpec
     */
    void assets(Closure configure) {
        if (assets == null) {
            assets = project.copySpec()
        }
        configure.delegate = assets
        configure.resolveStrategy = Closure.DELEGATE_FIRST
        configure()
    }

    WroModel createWroModel() {
        def wro = new WroModel()
        bundles.values().each { bundle ->
            wro.addGroup(bundle.group)
        }
        return wro
    }

    CopySpec getAssets() {
        return assets
    }

    static WebResourceSet get(Project project) {
        return project.extensions.getByType(WebResourceSet)
    }
}
