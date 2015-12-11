package ro.isdc.wro4j.extensions;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.support.CssUrlInspector;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;

public class CssUrlUnrootPostProcessor implements ResourcePostProcessor {
    public static final String ALIAS = "cssUrlUnroot";

    private static final Logger LOG = LoggerFactory.getLogger(CssUrlUnrootPostProcessor.class);
    private static final String ROOT = "/";

    @Override
    public void process(Reader reader, Writer writer) throws IOException {
        LOG.debug("Unrooting CSS url-s...");
        try {
            String css = IOUtils.toString(reader);
            String result = new CssUrlInspector().findAndReplace(css, createUrlHandler());

            writer.write(result);
        } finally {
            reader.close();
            writer.close();
        }
        LOG.debug("...CSS url-s successfully replaced.");
    }

    private CssUrlInspector.ItemHandler createUrlHandler() {
        return new CssUrlInspector.ItemHandler() {
            public String replace(String originalDeclaration, String originalUrl) {
                String modifiedDeclaration = originalDeclaration;
                if (originalUrl.startsWith(ROOT)) {
                    try {
                        String modifiedUrl = new URI(originalUrl.substring(1)).normalize().getPath();

                        modifiedDeclaration = Matcher.quoteReplacement(
                            originalDeclaration.replace(originalUrl, modifiedUrl)
                        );
                    } catch (URISyntaxException e) {
                        throw new WroRuntimeException(e.getMessage(), e);
                    }
                }
                return modifiedDeclaration;
            }
        };
    }
}
