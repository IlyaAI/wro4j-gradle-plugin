package ro.isdc.wro4j.gradle.tasks
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.mockito.Mockito
import ro.isdc.wro.WroRuntimeException
import ro.isdc.wro.config.Context
import ro.isdc.wro.config.jmx.WroConfiguration
import ro.isdc.wro.http.support.DelegatingServletOutputStream
import ro.isdc.wro.manager.WroManager
import ro.isdc.wro.manager.factory.WroManagerFactory
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.util.io.UnclosableBufferedInputStream
import ro.isdc.wro4j.gradle.Wro4JSettings

import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

public class OptimizeResourcesTask extends DefaultTask {
    private Wro4JSettings settings;

    OptimizeResourcesTask() {
    }

    private Wro4JSettings getSettings() {
        return settings != null ? settings : (settings = Wro4JSettings.get(project))
    }

    @Input
    Class<? extends WroManagerFactory> getWroManagerFactory() {
        return getSettings().wroManagerFactory
    }

    void setWroManagerFactory(Class<? extends WroManagerFactory> factoryClass) {
        getSettings().wroManagerFactory = factoryClass
    }

    @InputFile
    File getWroFile() {
        return getSettings().wroFile
    }

    void setWroFile(File file) {
        getSettings().wroFile = file
    }

    @Input
    Set<String> getTargetGroups() {
        return getSettings().targetGroups;
    }

    void setTargetGroup(Set<String> groups) {
        getSettings().targetGroups = groups
    }

    void targetGroup(String group) {
        getSettings().targetGroups += group
    }

    @InputDirectory
    File getContextFolder() {
        return getSettings().contextFolder
    }

    void setContextFolder(File src) {
        getSettings().contextFolder = src
    }

    @OutputDirectory
    File getDestinationFolder() {
        return getSettings().destinationFolder
    }

    void setDestinationFolder(File dst) {
        getSettings().destinationFolder = dst
    }

    @OutputDirectory
    File getJsDestinationFolder() {
        return getSettings().jsDestinationFolder
    }

    void setJsDestinationFolder(File dstJs) {
        getSettings().jsDestinationFolder = dstJs
    }

    @OutputDirectory
    File getCssDestinationFolder() {
        return getSettings().cssDestinationFolder
    }

    void setCssDestinationFolder(File dstCss) {
        getSettings().cssDestinationFolder = dstCss;
    }
    
    @TaskAction
    private void optimize() {
        getLogger().debug("Instantiating WroManager via '{}' factory...", getSettings().wroManagerFactory)
        def wroManager = ((WroManagerFactory) getSettings().wroManagerFactory.newInstance()).create()

        for (def group: getSettings().targetGroups) {
            for (def resourceType: ResourceType.values()) {
                def dstFolder = getDestinationFolderFor(resourceType)
                def groupWithExt = group + "." + resourceType.name().toLowerCase()

                processGroup(wroManager, groupWithExt, dstFolder)
            }
        }
    }

    private void processGroup(WroManager wroManager, String group, File dstFolder) {
        getLogger().info("Processing group '{}'...", group)

        def request = Mockito.mock(HttpServletRequest)
        Mockito.when(request.getContextPath())
                .thenReturn(getSettings().contextFolder.path)
        Mockito.when(request.getRequestURI())
                .thenReturn(group)

        def output = new ByteArrayOutputStream()
        def response = Mockito.mock(HttpServletResponse)
        Mockito.when(response.getOutputStream())
                .thenReturn(new DelegatingServletOutputStream(output))

        def config = new WroConfiguration()
        // plugin should ignore empty groups, since it will try to process all types of resources
        config.setIgnoreEmptyGroup(true)

        def ctx = Context.webContext(request, response, Mockito.mock(FilterConfig))
        //ctx.aggregatedFolderPath = getAggregatedPathResolver().resolve()
        Context.set(ctx, config)

        getLogger().info("  applying pre- and post-processors", group)
        wroManager.process()

        if (output.size() == 0) {
            getLogger().info("There is no content generated. Skipping empty group.")
        } else {
            def input = new UnclosableBufferedInputStream(output.toByteArray())
            def stampedName = wroManager.getNamingStrategy().rename(group, input)
            def destinationFile = new File(dstFolder, stampedName)

            new FileOutputStream(destinationFile).withStream {
                input.reset()
                IOUtils.copy(input, it)
            };

            getLogger().info("There are {}KB generated into '{}'.", output.size() / 1024, destinationFile)
        }
    }

    private File getDestinationFolderFor(ResourceType resourceType) {
        File folder = getSettings().destinationFolder;

        switch (resourceType) {
            case ResourceType.JS:
                if (getSettings().jsDestinationFolder != null) {
                    folder = getSettings().jsDestinationFolder
                }
                break;
            case ResourceType.CSS:
                if (getSettings().cssDestinationFolder != null) {
                    folder = getSettings().cssDestinationFolder
                }
                break;
        }

        if (folder == null) {
            throw new WroRuntimeException(
                String.format(
                    "Couldn't deduce '%s' destination folder. Hint: define eighther destinationFolder, cssDestinationFolder or jsDestinationFolder.",
                    resourceType)
            )
        }

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder;
    }
}
