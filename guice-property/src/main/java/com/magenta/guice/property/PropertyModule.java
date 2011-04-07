package com.magenta.guice.property;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provider;
import com.magenta.guice.property.converter.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.magenta.guice.property.converter.ArrayTypeConverter.*;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:20:38
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class PropertyModule extends AbstractModule {

    private final PropertiesHandler propertiesHandler;

    public PropertyModule(PropertiesHandler propertiesHandler) {
        this.propertiesHandler = propertiesHandler;
    }

    public PropertyModule(Map<String, String> propertiesMap) {
        this.propertiesHandler = new MapPropertiesHandler(propertiesMap);
    }

    public PropertyModule(Properties properties) {
        this.propertiesHandler = new JavaPropertiesHandler(properties);
    }

    private static void bindTypes(Binder binder) {
        //array converter
        binder.convertToTypes(new ArrayMatcher(String.class), STRING_ARRAY_CONVERTER);
        binder.convertToTypes(new ArrayMatcher(int.class), INT_ARRAY_CONVERTER);
        binder.convertToTypes(new ArrayMatcher(boolean.class), BOOLEAN_ARRAY_CONVERTER);
        binder.convertToTypes(new ArrayMatcher(double.class), DOUBLE_ARRAY_CONVERTER);
        //files
        binder.convertToTypes(new ClazzMatcher(File.class), new FileTypeConverter());
        //URL
        binder.convertToTypes(new ClazzMatcher(URL.class), new URLTypeConverter());
        //URI
        binder.convertToTypes(new ClazzMatcher(URI.class), new URITypeConverter());
        //DateFormat
        binder.convertToTypes(new ClazzMatcher(DateFormat.class), new DateFormatTypeConverter());
        //Date
        binder.convertToTypes(new ClazzMatcher(Date.class), new DateTypeConverter());
    }

    private static void bindProperties(Binder binder, final PropertiesHandler propertiesHandler) {
        for (final String key : propertiesHandler.keys()) {
            binder.bindConstant().annotatedWith(new PropertyImpl(key)).to(new Provider<String>() {
                public String get() {
                    return propertiesHandler.get(key);
                }
            });
        }
    }

    public static void bindProperties(Binder binder, Map<String, String> propertiesMap) {
        bindProperties(binder, new MapPropertiesHandler(propertiesMap));
    }

    public static void bindProperties(Binder binder, Properties properties) {
        bindProperties(binder, new JavaPropertiesHandler(properties));
    }

    @Override
    protected void configure() {
        bindProperties(binder(), propertiesHandler);
        bindTypes(binder());
    }

    public static class JavaPropertiesHandler implements PropertiesHandler {
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

    public static class MapPropertiesHandler implements PropertiesHandler {
        private final Map<String, String> propertiesMap;

        public MapPropertiesHandler(Map<String, String> propertiesMap) {
            this.propertiesMap = propertiesMap;
        }

        public Set<String> keys() {
            return propertiesMap.keySet();
        }

        public String get(String key) {
            return propertiesMap.get(key);
        }
    }
}
