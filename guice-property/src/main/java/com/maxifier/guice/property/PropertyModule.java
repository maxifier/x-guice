package com.maxifier.guice.property;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.maxifier.guice.property.converter.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.maxifier.guice.property.converter.ArrayTypeConverter.*;

/**
 * Binds module properties and type converters
 * <p>Module should be constructed using one of {@code #loadFrom()} methods.
 * {@link #loadFrom(Module)} and {@link #loadFrom(Class)} tries to load properties from resource
 * with the same name as a module class. To specify custom resource name use {@link #loadFrom(String)}
 * or provide resource as {@code InputStream} or {@code Reader}.</p>
 * <p>Properties and it's comments will be read using {@link PropertyReader}.
 * Property comments can be parsed later to extract annotations by
 * {@link #getPropertyAnnotation(PropertyDefinition, String)}.</p>
 * <p>PropertyModule instance binds property values using {@link Property} annotation.
 * Module binds itself too to provide property metadata for other tools.
 * Take property definitions by {@link #getProperties()}.</p>
 * <p>To bind extended type converters call {@link #withConverters()} on module.</p>
 *
 * @author Konstantin Lyamshin (2015-12-29 2:04)
 */
@ParametersAreNonnullByDefault
public class PropertyModule implements Module {
    private final ImmutableList<PropertyDefinition> properties;
    private final String source;
    private boolean bindConverters;

    /**
     * @return general @Property instance
     */
    @Nonnull
    public static Property property(String name) {
        return new PropertyImpl(checkNotNull(name));
    }

    /**
     * @return certain property binding key
     */
    @Nonnull
    public static Key<String> propertyKey(String name) {
        return Key.get(String.class, property(name));
    }

    /**
     * Load properties from resource {@code module.getClass().getProperty() + ".properties"}
     */
    public static PropertyModule loadFrom(Module module) {
        return loadFrom(module.getClass());
    }

    /**
     * Read properties from classpath resource {@code moduleClass.getProperty() + ".properties"}
     */
    public static PropertyModule loadFrom(Class<? extends Module> moduleClass) {
        String resourceName = moduleClass.getSimpleName() + ".properties";
        InputStream inputStream = moduleClass.getResourceAsStream(resourceName);
        if (inputStream == null) { // try w/o package name
            inputStream = moduleClass.getResourceAsStream("/" + resourceName);
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("Can't load resource " + resourceName);
        }
        try {
            try {
                return new PropertyModule(
                    ImmutableSet.copyOf(loadProperties0(new PropertyReader(inputStream))), moduleClass.getSimpleName()
                );
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't read properties from " + resourceName, e);
        }
    }

    /**
     * Read properties from classpath resource by name
     */
    public static PropertyModule loadFrom(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = PropertyModule.class.getClassLoader();
        }
        InputStream inputStream = classLoader.getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new IllegalArgumentException("Can't load resource " + resourceName);
        }
        try {
            try {
                return new PropertyModule(
                    ImmutableSet.copyOf(loadProperties0(new PropertyReader(inputStream))), resourceName
                );
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't read properties from " + resourceName, e);
        }
    }

    /**
     * Reads properties from provided {@link InputStream} using ISO8859_1 encoding
     */
    public static PropertyModule loadFrom(InputStream inputStream) {
        try {
            try {
                return new PropertyModule(
                    ImmutableSet.copyOf(loadProperties0(new PropertyReader(inputStream))), ""
                );
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't read properties from InputStream", e);
        }
    }

    /**
     * Reads properties from provided {@link Reader}
     */
    public static PropertyModule loadFrom(Reader reader) {
        try {
            try {
                return new PropertyModule(
                    ImmutableSet.copyOf(loadProperties0(new PropertyReader(reader))), ""
                );
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't read properties from Reader", e);
        }
    }

    /**
     * Use custom properties
     */
    public static PropertyModule loadFrom(Properties properties) {
        ImmutableSet.Builder<PropertyDefinition> definitions = ImmutableSet.builder();
        for (String key : properties.stringPropertyNames()) {
            definitions.add(new PropertyDefinition(key, properties.getProperty(key), ""));
        }
        return new PropertyModule(definitions.build(), "");
    }

    /**
     * Use custom properties
     */
    public static PropertyModule loadFrom(Map<String, String> properties) {
        ImmutableSet.Builder<PropertyDefinition> definitions = ImmutableSet.builder();
        for (String key : properties.keySet()) {
            definitions.add(new PropertyDefinition(key, properties.get(key), ""));
        }
        return new PropertyModule(definitions.build(), "");
    }

    /**
     * Load properties from custom definitions
     */
    public static PropertyModule loadFrom(Set<PropertyDefinition> properties) {
        return new PropertyModule(properties, "");
    }

    /**
     * Parses property comment and extract specific annotation value.
     * <p>If there are more than one annotation with same name, only first one will be returned.</p>
     * <p>Annotation name should include '@' sign.</p>
     * <p>Whitespaces are trimmed from annotation value.</p>
     *
     * @see PropertyAnnotationParser
     * @param definition property definition
     * @param annotation desired annotation (including '@' sign)
     * @return annotation value or null if not found
     */
    @Nullable
    public static String getPropertyAnnotation(PropertyDefinition definition, String annotation) {
        String result = null;
        PropertyAnnotationParser parser = new PropertyAnnotationParser(definition.getComment());
        while (parser.hasNext()) {
            Map.Entry<String, String> entry = parser.next();
            if (entry.getKey().equals(annotation)) {
                result = entry.getValue();
            }
        }
        return result;
    }

    /**
     * Reads properties from input stream using ISO8859_1 encoding.
     * <p>Data format is similar to original {@link Properties#load(InputStream)} format.</p>
     *
     * @param inputStream the input stream
     * @throws IOException input stream can't be read
     */
    public static List<PropertyDefinition> loadProperties(InputStream inputStream) throws IOException {
        return loadProperties0(new PropertyReader(inputStream));
    }

    /**
     * Reads properties from character reader.
     * <p>Data format is similar to original {@link Properties#load(Reader)})} format.</p>
     *
     * @param reader input reader
     * @throws IOException input stream can't be read
     */
    public static List<PropertyDefinition> loadProperties(Reader reader) throws IOException {
        return loadProperties0(new PropertyReader(reader));
    }

    private static List<PropertyDefinition> loadProperties0(PropertyReader reader) throws IOException {
        ArrayList<PropertyDefinition> properties = new ArrayList<PropertyDefinition>();
        while (!reader.isEof()) {
            String comment = reader.readComment();
            String key = reader.readKey();
            if (key != null) {
                String value = reader.readValue();
                properties.add(new PropertyDefinition(key, nullToEmpty(value), nullToEmpty(comment)));
            }
        }
        return properties;
    }

    private PropertyModule(Set<PropertyDefinition> properties, String source) {
        this.source = source;
        this.properties = ImmutableList.copyOf(properties);
    }

    /**
     * Binds custom type converters too
     */
    public PropertyModule withConverters() {
        this.bindConverters = true;
        return this;
    }

    /**
     * @return list of properties to bind
     */
    @Nonnull
    public ImmutableList<PropertyDefinition> getProperties() {
        return properties;
    }

    @Override
    public void configure(Binder binder) {
        binder = binder.skipSources(PropertyModule.class);

        // Bind module itself for later collection of properties
        binder.bind(PropertyModule.class).annotatedWith(property(UUID.randomUUID().toString())).toInstance(this);

        for (PropertyDefinition prop : properties) {
            binder.bindConstant().annotatedWith(property(prop.getName())).to(prop.getValue());
        }

        if (bindConverters) {
            bindConverters(binder);
        }
    }

    private void bindConverters(Binder binder) {
        binder.convertToTypes(new ArrayMatcher(String.class), STRING_ARRAY_CONVERTER);
        binder.convertToTypes(new ArrayMatcher(int.class), INT_ARRAY_CONVERTER);
        binder.convertToTypes(new ArrayMatcher(boolean.class), BOOLEAN_ARRAY_CONVERTER);
        binder.convertToTypes(new ArrayMatcher(double.class), DOUBLE_ARRAY_CONVERTER);
        binder.convertToTypes(new ClazzMatcher(File.class), new FileTypeConverter());
        binder.convertToTypes(new ClazzMatcher(URL.class), new URLTypeConverter());
        binder.convertToTypes(new ClazzMatcher(URI.class), new URITypeConverter());
        binder.convertToTypes(new ClazzMatcher(DateFormat.class), new DateFormatTypeConverter());
        binder.convertToTypes(new ClazzMatcher(Date.class), new DateTypeConverter());
    }

    @Override
    public String toString() {
        return String.format("PropertyModule{%s}", source);
    }

    @SuppressWarnings("ClassExplicitlyAnnotation")
    private static class PropertyImpl implements Property, Serializable {
        private static final long serialVersionUID = 3672977046537086190L;
        private final String value;

        private PropertyImpl(String name) {
            this.value = name;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Property.class;
        }

        @Override
        public int hashCode() {
            // This is specified in java.lang.Annotation.
            return (127 * "value".hashCode()) ^ value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Property) {
                Property other = (Property) o;
                return value.equals(other.value());
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("@Property(%s)", value);
        }
    }
}
