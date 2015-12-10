package ro.isdc.wro4j.gradle

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.mockito.Matchers
import org.mockito.Mockito
import ro.isdc.wro.config.Context
import ro.isdc.wro.config.jmx.WroConfiguration
import ro.isdc.wro.http.support.DelegatingServletOutputStream
import ro.isdc.wro.manager.WroManager
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.model.resource.locator.factory.ConfigurableLocatorFactory
import ro.isdc.wro.model.resource.processor.factory.ConfigurableProcessorsFactory
import ro.isdc.wro.util.io.UnclosableBufferedInputStream

import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

public class WebCompileTask extends DefaultTask {
    private WroModel wroModel
    private Set<String> targetGroups = []
    private List<String> uriLocators = ["servletContext"]
    private List<String> preProcessors = []
    private List<String> postProcessors = []
    private File sourcesDir;
    private File outputDir;

    WebCompileTask() {
        wroModel = new WroModel()
    }

    @Input
    String getWroModelHash() {
        return wroModel.toString()
    }

    WroModel getWroModel() {
        return wroModel
    }

    void setWroModel(WroModel wroModel) {
        this.wroModel = wroModel
    }

    @Input
    Set<String> getTargetGroups() {
        return targetGroups
    }

    void setTargetGroups(Set<String> groups) {
        targetGroups = groups
    }

    @Input
    List<String> getPreProcessors() {
        return preProcessors
    }

    void setPreProcessors(List<String> pre) {
        preProcessors = pre
    }

    @Input
    List<String> getPostProcessors() {
        return postProcessors
    }

    void setPostProcessors(List<String> post) {
        postProcessors = post
    }

    @InputDirectory
    File getSourcesDir() {
        return sourcesDir
    }

    void setSourcesDir(File src) {
        sourcesDir = src
    }

    @OutputDirectory
    File getOutputDir() {
        return outputDir
    }

    void setOutputDir(File dst) {
        outputDir = dst
    }

    @TaskAction
    private void compile() {
        for (def group: targetGroups) {
            for (def resourceType: ResourceType.values()) {
                def groupWithExt = group + "." + resourceType.name().toLowerCase()
                processGroup(groupWithExt)
            }
        }
    }

    private WroManager createWroManager() {
        def configProps = createConfigProperties()
        def factory = new EmbeddedWroManagerFactory(wroModel, configProps)

        return factory.create()
    }

    private Properties createConfigProperties() {
        def props = new Properties()

        props.setProperty(ConfigurableLocatorFactory.PARAM_URI_LOCATORS, uriLocators.join(","));
        props.setProperty(ConfigurableProcessorsFactory.PARAM_PRE_PROCESSORS, preProcessors.join(","))
        props.setProperty(ConfigurableProcessorsFactory.PARAM_POST_PROCESSORS, postProcessors.join(","))

        return props
    }

    private void processGroup(String group) {
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
        Mockito.when(request.getContextPath()).thenReturn(".")
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
        // plugin should ignore empty groups, since it will try to process all types of resources
        config.setIgnoreEmptyGroup(true)

        def ctx = Context.webContext(request, response, filterConfig)
        ctx.aggregatedFolderPath = ""

        Context.set(ctx, config)
        try {
            getLogger().debug("  initiating WroManager")
            def wroManager = createWroManager()

            getLogger().debug("  applying pre- and post-processors")
            wroManager.process()

            if (output.size() == 0) {
                getLogger().info("There is no content generated. Skipping empty group.")
            } else {
                def input = new UnclosableBufferedInputStream(output.toByteArray())
                def stampedName = wroManager.getNamingStrategy().rename(group, input)
                def destinationFile = new File(outputDir, stampedName)

                outputDir.mkdirs()

                new FileOutputStream(destinationFile).withStream {
                    input.reset()
                    IOUtils.copy(input, it)
                };

                getLogger().info("There are {}B generated into '{}'.", output.size(), destinationFile)
            }
        } finally {
            Context.unset()
        }
    }
}
