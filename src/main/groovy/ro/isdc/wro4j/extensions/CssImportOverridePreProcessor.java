package ro.isdc.wro4j.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.model.resource.processor.impl.css.LessCssImportPreProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CssImportOverridePreProcessor extends LessCssImportPreProcessor {
    public static final String ALIAS = "cssImportOverride";

    private final static Logger LOG = LoggerFactory.getLogger(CssImportOverridePreProcessor.class);

    private final Map<String, String> overrides = new HashMap<String, String>();

    public CssImportOverridePreProcessor(Properties props) {
        LOG.debug("overrides {");
        for (String key: props.stringPropertyNames()) {
            String from = decodeKey(key);
            if (from == null) {
                continue;
            }

            String to = props.getProperty(key);
            overrides.put(from, to);
            LOG.debug("  {} -> {}", from, to);
        }
        LOG.debug("}");
    }

    @Override
    protected List<String> findImports(String css) {
        List<String> imports = super.findImports(css);

        int overriddenCount = 0;
        for (int i = 0; i < imports.size(); i++) {
            String importUri = imports.get(i);
            String mapTo = overrides.get(importUri);
            if (mapTo != null) {
                imports.set(i, mapTo);
                overriddenCount++;
            }
        }

        LOG.debug("{} import(s) have been overridden.", overriddenCount);

        return imports;
    }

    public static String encodeKey(String from) {
        return ALIAS + "." + from;
    }

    private static String decodeKey(String key) {
        return key.startsWith(ALIAS)
            ? key.substring(ALIAS.length() + 1)
            : null;
    }
}
