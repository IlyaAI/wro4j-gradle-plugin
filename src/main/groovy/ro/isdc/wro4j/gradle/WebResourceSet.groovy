package ro.isdc.wro4j.gradle

import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import ro.isdc.wro.model.WroModel

class WebResourceSet {
    private static final String STATIC = "static"
    static final String NAME = "webResources"


    private final Map<String, WebBundle> bundles = new HashMap<>()
    private final CopySpec assets;
    private File srcDir
    private String staticFolder

    WebResourceSet(Project project) {
        assets = project.copySpec()
        srcDir = project.file("src/main/webResources")
        staticFolder = STATIC
    }

    File getSrcDir() {
        return srcDir
    }

    void setSrcDir(File srcDir) {
        this.srcDir = srcDir
    }

    String getStaticFolder() {
        return staticFolder
    }

    void setStaticFolder(String staticFolder) {
        this.staticFolder = staticFolder
    }

    Collection<WebBundle> getBundles() {
        return Collections.unmodifiableCollection(bundles.values())
    }

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

    void assets(Closure configure) {
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

    CopySpec getAssetsSpec() {
        return assets
    }

    static WebResourceSet get(Project project) {
        return project.extensions.getByType(WebResourceSet)
    }
}
