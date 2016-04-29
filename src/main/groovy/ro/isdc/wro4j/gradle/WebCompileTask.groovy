package ro.isdc.wro4j.gradle

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.mockito.Matchers
import org.mockito.Mockito
import ro.isdc.wro.config.Context
import ro.isdc.wro.config.jmx.WroConfiguration
import ro.isdc.wro.http.support.DelegatingServletOutputStream
import ro.isdc.wro.manager.WroManager
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.group.Group
import ro.isdc.wro.model.resource.Resource
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator
import ro.isdc.wro.model.resource.locator.factory.ConfigurableLocatorFactory
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory
import ro.isdc.wro.model.resource.processor.support.LessCssImportInspector
import ro.isdc.wro.util.io.UnclosableBufferedInputStream
import ro.isdc.wro4j.extensions.CssImportOverridePreProcessor

import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.zip.GZIPOutputStream

public class WebCompileTask extends DefaultTask {
    private final List<String> uriLocators = ["servletContext"]
    private WebBundle bundle
    private File sourcesDir
    private File outputDir

    WebCompileTask() {
    }

    @Nested
    WebBundle getBundle() {
        if (bundle == null) {
            throw new IllegalStateException("Web resources bundle wasn't provided.")
        }
        return bundle
    }

    void setBundle(WebBundle bundle) {
        this.bundle = bundle
    }

    File getSourcesDir() {
        return sourcesDir
    }

    void setSourcesDir(File src) {
        sourcesDir = src
    }

    File getOutputDir() {
        return outputDir
    }

    void setOutputDir(File dst) {
        outputDir = dst
    }

    @InputFiles
    protected Set<File> getSourceFiles() {
        def bundle = getBundle()

        def srcFiles = new HashSet<File>()
        srcFiles.addAll(
            bundle.getJsPaths(sourcesDir).collect{ it.getFile(sourcesDir) }
        )

        for (def cssPath: bundle.getCssPaths(sourcesDir)) {
            def file = cssPath.getFile(sourcesDir)
            srcFiles.add(file)
            resolveCssImports(file, srcFiles)
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("{}.sourceFiles {", name)
            srcFiles.each { getLogger().debug("  {}", it) }
            getLogger().debug("}")
        }

        return srcFiles
    }

    private void resolveCssImports(File file, Set<File> srcFiles) {
        def parent = file.parentFile
        def props = getBundle().configProperties
        def imports = new LessCssImportInspector(file.getText()).findImports()

        for (def cssPath: imports) {
            def mapTo = props.get(CssImportOverridePreProcessor.encodeKey(cssPath));
            if (mapTo != null) {
                cssPath = mapTo
            }

            def importedFile = new File(parent, cssPath)
            srcFiles.add(importedFile)
            resolveCssImports(importedFile, srcFiles)
        }
    }

    @OutputFiles
    protected Set<File> getOutputFiles() {
        def bundle = getBundle()

        def outFiles = new HashSet<File>()
        if (bundle.hasJs) {
            def jsName = jsNameOf(bundle);
            outFiles.add(new File(outputDir, jsName))

            if (bundle.gzipped) {
                outFiles.add(new File(outputDir, gzNameOf(jsName)))
            }
        }
        if (bundle.hasCss) {
            def cssName = cssNameOf(bundle);
            outFiles.add(new File(outputDir, cssName))

            if (bundle.gzipped) {
                outFiles.add(new File(outputDir, gzNameOf(cssName)))
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("{}.outputFiles {", name)
            outFiles.each { getLogger().debug("  {}", it) }
            getLogger().debug("}")
        }

        return outFiles
    }

    @TaskAction
    private void compile() {
        def bundle = getBundle()
        def wroModel = createWroModel()

        if (bundle.hasJs) {
            processGroup(wroModel, jsNameOf(bundle), bundle.gzipped)
        }
        if (bundle.hasCss) {
            processGroup(wroModel, cssNameOf(bundle), bundle.gzipped)
        }
    }

    private WroModel createWroModel() {
        def bundle = getBundle()

        def wro = new WroModel()
        def group = new Group(bundle.name)
        for (def jsPath: bundle.getJsPaths(sourcesDir)) {
            group.addResource(Resource.create(uriOf(jsPath.pathString), ResourceType.JS))
        }
        for (def cssPath: bundle.getCssPaths(sourcesDir)) {
            group.addResource(Resource.create(uriOf(cssPath.pathString), ResourceType.CSS))
        }
        wro.addGroup(group)

        if (getLogger().isInfoEnabled()) {
            getLogger().info("{}.WroModel '{}' {", name, bundle.name)
            group.resources.each { getLogger().info("  {}", it.uri) }
            getLogger().info("}")
        }

        return wro
    }

    private WroManager createWroManager(WroModel wroModel) {
        def configProps = createConfigProperties()
        def factory = new EmbeddedWroManagerFactory(wroModel, configProps)

        return factory.create()
    }

    private Properties createConfigProperties() {
        def bundle = getBundle()
        def props = new Properties()

        props.setProperty(ConfigurableLocatorFactory.PARAM_URI_LOCATORS, uriLocators.join(","));
        props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, bundle.preProcessors.join(","))
        props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, bundle.postProcessors.join(","))

        for (Map.Entry<String, String> entry: bundle.configProperties) {
            props.setProperty(entry.key, entry.value)
        }

        return props
    }

    private void processGroup(WroModel wroModel, String group, boolean gzipped) {
        getLogger().info("Processing group '{}'...", group)

        def requestUrl = new StringBuffer()
        requestUrl.append(sourcesDir.toURI().toURL())

        def servletContext = Mockito.mock(ServletContext)
        Mockito.when(servletContext.getRealPath(Matchers.anyString()))
            .thenAnswer({invocation ->
                def vpath = (String)invocation.arguments[0]
                new File(sourcesDir, StringUtils.removeStart(vpath, "/")).path
            })

        def request = Mockito.mock(HttpServletRequest)
        Mockito.when(request.getContextPath()).thenReturn("")
        Mockito.when(request.getServletPath()).thenReturn("")
        Mockito.when(request.getRequestURI()).thenReturn(group)
        Mockito.when(request.getRequestURL()).thenReturn(requestUrl)

        def output = new ByteArrayOutputStream()
        def servletOutput = new DelegatingServletOutputStream(output)
        def response = Mockito.mock(HttpServletResponse)
        Mockito.when(response.getOutputStream()).thenReturn(servletOutput)

        def filterConfig = Mockito.mock(FilterConfig)
        Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext)

        def config = new WroConfiguration()
        def ctx = Context.webContext(request, response, filterConfig)
        ctx.aggregatedFolderPath = ""

        Context.set(ctx, config)
        try {
            getLogger().debug("  initiating WroManager")
            def wroManager = createWroManager(wroModel)

            getLogger().debug("  applying pre- and post-processors")
            wroManager.process()

            def input = new UnclosableBufferedInputStream(output.toByteArray())
            def stampedName = wroManager.getNamingStrategy().rename(group, input)
            def destinationFile = new File(outputDir, stampedName)

            outputDir.mkdirs()

            new FileOutputStream(destinationFile).withStream {
                input.reset()
                IOUtils.copy(input, it)
            };

            getLogger().info("{} => {} / {}B", group, destinationFile, destinationFile.length())

            if (gzipped) {
                getLogger().debug("  compressing")

                destinationFile = new File(outputDir, gzNameOf(stampedName))

                new GZIPOutputStream(
                    new FileOutputStream(destinationFile)
                ).withStream {
                    input.reset()
                    IOUtils.copy(input, it)
                };

                getLogger().info("  => {} / {}B", destinationFile, destinationFile.length())
            }
        } finally {
            Context.unset()
        }
    }

    private static String jsNameOf(WebBundle bundle) {
        return bundle.name + ".js"
    }

    private static String cssNameOf(WebBundle bundle) {
        return bundle.name + ".css"
    }

    private static String gzNameOf(String baseName) {
        return baseName + ".gz"
    }

    private static String uriOf(String resource) {
        if (resource.startsWith(ServletContextUriLocator.PREFIX)) {
            return resource
        }

        return ServletContextUriLocator.PREFIX + resource
    }
}
