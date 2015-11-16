package com.maxifier.guice.property;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.maxifier.guice.property.converter.*;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.maxifier.guice.property.converter.ArrayTypeConverter.*;

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


    /**
     * Bind types converters, could be called once per injector
     *
     * @param binder - binder of your module
     */
    public static void bindTypes(Binder binder) {
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

    /**
     * Bind properties from Java Properties class
     *
     * @param binder     - your module binder
     * @param properties - properties instance
     */
    public static void bindProperties(Binder binder, final Properties properties) {
        bindProperties(binder, new JavaPropertiesHandler(properties));
    }

    /**
     * Bind properties from Map
     *
     * @param binder        - your module binder
     * @param propertiesMap - map of properties
     */
    public static void bindProperties(Binder binder, final Map<String, String> propertiesMap) {
        bindProperties(binder, new MapPropertiesHandler(propertiesMap));
    }

    /**
     * Bind properties from PropertiesHandler interface implementation
     *
     * @param binder            - your module binder
     * @param propertiesHandler - PropertiesHandler implementation instance
     */
    public static void bindProperties(Binder binder, final PropertiesHandler propertiesHandler) {
        for (final String key : propertiesHandler.keys()) {
            binder.bindConstant().annotatedWith(new PropertyImpl(key)).to((String) propertiesHandler.get(key));
        }
    }

    /**
     * Bind JVM System properties.
     *
     * @param binder - your module binder
     */
    public static void bindSystemProperties(Binder binder) {
        bindProperties(binder, System.getProperties());
    }

    //deprecated, leaved for compatibility reason
    @Deprecated
    private final PropertiesHandler propertiesHandler;

    @Override
    protected void configure() {
        bindProperties(binder(), propertiesHandler);
    }

    @Deprecated //use static methods instead
    public PropertyModule(PropertiesHandler propertiesHandler) {
        this.propertiesHandler = propertiesHandler;
    }

    @Deprecated //use static methods instead
    public PropertyModule(Map<String, String> propertiesMap) {
        this.propertiesHandler = new MapPropertiesHandler(propertiesMap);
    }

    @Deprecated //use static methods instead
    public PropertyModule(Properties properties) {
        this.propertiesHandler = new JavaPropertiesHandler(properties);
    }

    static class MapPropertiesHandler implements PropertiesHandler {
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
