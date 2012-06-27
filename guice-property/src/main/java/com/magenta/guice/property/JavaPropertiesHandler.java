package com.magenta.guice.property;

import java.util.Properties;
import java.util.Set;

/**
 * Class wrapper for {@link java.util.Properties} to provide {@link com.magenta.guice.property.PropertiesHandler}
 * @author Igor Yankov (igor.yankov@maxifier.com)
 */
public class JavaPropertiesHandler implements PropertiesHandler {
    private final Properties properties;

    public JavaPropertiesHandler(Properties properties) {
        this.properties = properties;
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    //one way to do recast
    public Set<String> keys() {
        return (Set) properties.keySet();
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}
