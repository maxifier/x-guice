/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionPoint;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.util.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link Registry} implementation which supports collecting, overriding and interpolation of properties.
 * <p>Use {@link Builder} to construct new {@code CascadeRegistry}.</p>
 * <p>Property value flow: default->overrider->interpolator. Overriding occurred while construction.
 * Interpolation occurred each time on get value, so changes in one property reflects in dependent properties.</p>
 * <p>Properties without default values will be ignored.</p>
 * <p>Properties can be changed dynamically by {@link #set(String, String)}, changed values
 * passed to {@link Persister} on {@link #store()}.</p>
 *
 * @author Konstantin Lyamshin (2015-12-28 19:01)
 */
@ParametersAreNonnullByDefault
public class CascadeRegistry implements Registry {
    private static final Logger logger = LoggerFactory.getLogger(CascadeRegistry.class);
    private final Set<String> changedKeys = new HashSet<String>();
    private final ImmutableMap<String, PropertyDefinition> defaults;
    private final Map<String, String> values;
    private final Interpolator interpolator;
    private final Persister persister;

    protected CascadeRegistry(
        ImmutableMap<String, PropertyDefinition> defaults,
        Map<String, String> values,
        Interpolator interpolator,
        Persister persister
    ) {
        this.defaults = defaults;
        this.values = values;
        this.interpolator = interpolator;
        this.persister = persister;
    }

    @Nonnull
    @Override
    public Set<String> keys() {
        return defaults.keySet();
    }

    @Nullable
    @Override
    public synchronized String get(String key) {
        String value = values.get(key);
        return value != null? interpolator.interpolate(key, value, values): null;
    }

    @Override
    @Nonnull
    public synchronized String getOrDefault(String key, String defaultValue) {
        String value = values.get(key);
        return interpolator.interpolate(key, value != null? value: checkNotNull(defaultValue), values);
    }

    @Nullable
    public PropertyDefinition getDefinition(String key) {
        return defaults.get(key);
    }

    @Override
    public synchronized void set(String key, String value) {
        if (!defaults.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Property %s doesn't have declaration", key));
        }
        values.put(key, checkNotNull(value));
        changedKeys.add(key);
    }

    @Override
    public synchronized void store() {
        if (!changedKeys.isEmpty()) {
            ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
            for (String key : changedKeys) {
                map.put(key, values.get(key));
            }
            persister.persist(map.build());
            changedKeys.clear();
        }
    }

    /**
     * Apply overridden properties to provided module.
     * <p>All bindings of properties in provided module will be replaced with overridden values bindings.</p>
     */
    @Nonnull
    public Module applyOverrides(Module... modules) {
        ElementVisitor<Element> transformer = overrideTransformer();
        ArrayList<Element> elements = new ArrayList<Element>();
        for (Element element : Elements.getElements(modules)) {
            elements.add(element.acceptVisitor(transformer));
        }
        return Elements.getModule(elements);
    }

    /**
     * Apply overridden properties to provided module.
     * <p>All bindings of properties in provided module will be replaced with overridden values bindings.</p>
     */
    @Nonnull
    public Module applyOverrides(Iterable<? extends Module> modules) {
        ElementVisitor<Element> transformer = overrideTransformer();
        ArrayList<Element> elements = new ArrayList<Element>();
        for (Element element : Elements.getElements(modules)) {
            elements.add(element.acceptVisitor(transformer));
        }
        return Elements.getModule(elements);
    }

    /**
     * @return {@code ElementVisitor} which replaces property bindings with overridden property values
     */
    @Nonnull
    public ElementVisitor<Element> overrideTransformer() {
        return new DefaultElementVisitor<Element>() {
            @Override
            protected Element visitOther(Element element) {
                return element;
            }

            @Override
            public <T> Element visit(Binding<T> binding) {
                Key<T> key = binding.getKey();
                if (key.getTypeLiteral().getRawType() == String.class && key.getAnnotation() instanceof Property) {
                    @SuppressWarnings("unchecked") Binding<String> stringBinding = (Binding<String>) binding;
                    String value = get(((Property) key.getAnnotation()).value());
                    if (value != null) {
                        return new OverridenPropertyBinding(stringBinding, value);
                    }
                }
                return binding;
            }
        };
    }

    /**
     * @return Module which binds this registry instance
     */
    @Nonnull
    public Module asModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Registry.class).toInstance(CascadeRegistry.this);
            }
        };
    }

    /**
     * Use this class to build configured CascadeRegistry instance
     */
    public static class Builder {
        private ImmutableSet<PropertyDefinition> defaults = ImmutableSet.of();
        private Map<String, String> overrides = ImmutableMap.of();
        private Overrider overrider = NOP_OVERRIDER;
        private Interpolator interpolator = NOP_INTERPOLATOR;
        private Persister persister = NOP_PERSISTER;

        /**
         * Reads default property values from Guice {@code Module}.
         * <p>Properties should be installed using {@link PropertyModule}.</p>
         */
        public Builder withDefaults(Module module) {
            return withDefaults(Elements.getElements(module));
        }

        /**
         * Reads default property values from set of Guice bindings.
         * <p>Properties should be installed using {@link PropertyModule}.</p>
         */
        public Builder withDefaults(List<Element> module) {
            ElementVisitor<PropertyModule> visitor = new DefaultElementVisitor<PropertyModule>() {
                @Override
                public <T> PropertyModule visit(Binding<T> binding) {
                    if (binding.getKey().getAnnotation() instanceof Property) {
                        T value = binding.acceptTargetVisitor(new DefaultBindingTargetVisitor<T, T>() {
                            @Override
                            public T visit(InstanceBinding<? extends T> instanceBinding) {
                                return instanceBinding.getInstance();
                            }
                        });
                        if (value instanceof PropertyModule) {
                            return (PropertyModule) value;
                        }
                    }
                    return null;
                }
            };
            ImmutableSet.Builder<PropertyDefinition> definitions = ImmutableSet.builder();
            for (Element element : module) {
                PropertyModule propertyModule = element.acceptVisitor(visitor);
                if (propertyModule != null) {
                    definitions.addAll(propertyModule.getProperties());
                }
            }
            this.defaults = definitions.build();
            return this;
        }

        /**
         * Use provided {@link Properties} as default properties.
         * <p>Result properties won't have any annotations.</p>
         */
        public Builder withDefaults(Properties defaults) {
            ImmutableSet.Builder<PropertyDefinition> values = ImmutableSet.builder();
            for (String key : defaults.stringPropertyNames()) {
                values.add(new PropertyDefinition(
                    key, checkNotNull(defaults.getProperty(key), "Null property value for key '%s'", key),
                    ""
                ));
            }
            this.defaults = values.build();
            return this;
        }

        /**
         * Use provided {@link Map} as default properties.
         * <p>Result properties won't have any annotations.</p>
         */
        public Builder withDefaults(Map<String, String> defaults) {
            ImmutableSet.Builder<PropertyDefinition> values = ImmutableSet.builder();
            for (Map.Entry<String, String> entry : defaults.entrySet()) {
                values.add(new PropertyDefinition(
                    entry.getKey(), checkNotNull(entry.getValue(), "Null property value for key '%s'", entry.getKey()),
                    ""
                ));
            }
            this.defaults = values.build();
            return this;
        }

        /**
         * Use provided set of {@link PropertyDefinition}s as default properties.
         */
        public Builder withDefaults(Set<PropertyDefinition> defaults) {
            this.defaults = ImmutableSet.copyOf(defaults);
            return this;
        }

        /**
         * Use provided {@link Properties} as overrides.
         */
        public Builder withOverrides(Properties overrides) {
            ImmutableMap.Builder<String, String> values = ImmutableMap.builder();
            for (String key : overrides.stringPropertyNames()) {
                values.put(key, checkNotNull(overrides.getProperty(key), "Null property value for key '%s'", key));
            }
            this.overrides = values.build();
            return this;
        }

        /**
         * Use provided {@link Map} as overrides.
         */
        public Builder withOverrides(Map<String, String> overrides) {
            this.overrides = checkNotNull(overrides);
            return this;
        }

        /**
         * Use custom {@link Overrider}.
         */
        public Builder withOverrider(Overrider overrider) {
            this.overrider = checkNotNull(overrider);
            return this;
        }

        /**
         * Use {@link Overrider} which overrides values by System properties.
         */
        public Builder withSystemProperties() {
            this.overrider = overrider == ENV_OVERRIDER || overrider == SYSENV_OVERRIDER
                ? SYSENV_OVERRIDER: SYS_OVERRIDER;
            return this;
        }

        /**
         * Use {@link Overrider} which overrides values by process environment.
         */
        public Builder withEnvironmentProperties() {
            this.overrider = overrider == SYS_OVERRIDER || overrider == SYSENV_OVERRIDER
                ? SYSENV_OVERRIDER: ENV_OVERRIDER;
            return this;
        }

        /**
         * Use property value interpolation while resolving properties.
         */
        public Builder withInterpolation() {
            this.interpolator = new DefaultInterpolator();
            return this;
        }

        /**
         * Use property value interpolation while resolving properties.
         * <p>Limit iterpolation by only properties which have {@code marker} annotaion.</p>
         * <p>If marker starts from '!' then inverts selection.</p>
         */
        public Builder withInterpolation(String marker) {
            this.interpolator = new SelectiveInterpolator(marker, defaults);
            return this;
        }

        /**
         * Use custom {@link Interpolator} to resolve property values.
         */
        public Builder withInterpolation(Interpolator interpolator) {
            this.interpolator = checkNotNull(interpolator);
            return this;
        }

        /**
         * Use custom {@link Persister} to save property changes.
         */
        public Builder withPersister(Persister persister) {
            this.persister = checkNotNull(persister);
            return this;
        }

        /**
         * Build {@link CascadeRegistry}.
         */
        public CascadeRegistry build() {
            ImmutableMap.Builder<String, PropertyDefinition> defaults = ImmutableMap.builder();
            HashMap<String, String> values = new HashMap<String, String>(this.defaults.size());
            for (PropertyDefinition definition : this.defaults) {
                String key = definition.getName();
                String value = definition.getValue();
                String override = overrides.get(key);
                if (override == null) {
                    override = value;
                }
                String override2 = overrider.override(key, override);
                if (override2 == null) {
                    override2 = override;
                }

                defaults.put(key, definition);
                values.put(key, override2);
            }
            return new CascadeRegistry(defaults.build(), values, interpolator, persister);
        }
    }

    /**
     * Provides overridden property values
     */
    public interface Overrider {
        @Nullable
        String override(String key, String oldValue);
    }

    /**
     * Persist registry changes
     */
    public interface Persister {
        void persist(Map<String, String> properties);
    }

    /**
     * Interpolates property values
     */
    public interface Interpolator {
        @Nonnull
        String interpolate(String key, String value, Map<String, String> registry);
    }

    private static final Overrider NOP_OVERRIDER = new Overrider() {
        @Nullable
        @Override
        public String override(String key, String oldValue) {
            return null;
        }

        @Override
        public String toString() {
            return "NopOverrider";
        }
    };

    private static final Overrider ENV_OVERRIDER = new Overrider() {
        @Nullable
        @Override
        public String override(String key, String oldValue) {
            return System.getenv(key);
        }

        @Override
        public String toString() {
            return "EnvOverrider";
        }
    };

    private static final Overrider SYS_OVERRIDER = new Overrider() {
        @Nullable
        @Override
        public String override(String key, String oldValue) {
            return System.getProperty(key);
        }

        @Override
        public String toString() {
            return "SysOverrider";
        }
    };

    private static final Overrider SYSENV_OVERRIDER = new Overrider() {
        @Nullable
        @Override
        public String override(String key, String oldValue) {
            return System.getProperty(key, System.getenv(key));
        }

        @Override
        public String toString() {
            return "SysEnvOverrider";
        }
    };

    private static final Persister NOP_PERSISTER = new Persister() {
        @Override
        public void persist(Map<String, String> properties) {
            // do nothing
        }

        @Override
        public String toString() {
            return "NopPersister";
        }
    };

    private static final Interpolator NOP_INTERPOLATOR = new Interpolator() {
        @Nonnull
        @Override
        public String interpolate(String key, String value, Map<String, String> registry) {
            return value;
        }

        @Override
        public String toString() {
            return "NopInterpolator";
        }
    };

    public static class DefaultInterpolator implements Interpolator {
        private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{(\\w[\\w\\-\\.]*)\\}");
        private static final int MAX_NESTING = 5;
        private final Set<String> missed = Collections.synchronizedSet(new HashSet<String>());

        @Nonnull
        @Override
        public String interpolate(String key, String value, Map<String, String> registry) {
            Matcher m = PARAM_PATTERN.matcher(value);
            if (!m.find()) {
                return value;
            }

            try {
                StringBuilder result = new StringBuilder(value.length() * 2);
                int lastPosition = 0;
                do {
                    result.append(value, lastPosition, m.start());
                    doInterpolation(key, result, m, registry, MAX_NESTING);
                    lastPosition = m.end();
                } while (m.find());
                result.append(value, lastPosition, value.length());
                return result.toString();
            } catch (IllegalArgumentException e) {
                if (missed.add(key)) { // avoid log flood
                    logger.error("{} for string '{}' while resolving '{}'", e.getMessage(), value, key);
                }
                return value;
            }
        }

        void doInterpolation(String key, StringBuilder result, Matcher matcher, Map<String, String> registry, int nesting) {
            if (nesting == 0) {
                throw new IllegalArgumentException("Maximum property nesting exceeded");
            }

            String property = matcher.group(1);
            String value = registry.get(property);
            if (value != null) {
                int lastPosition = 0;
                for (Matcher m = PARAM_PATTERN.matcher(value); m.find(); lastPosition = m.end()) {
                    result.append(value, lastPosition, m.start());
                    doInterpolation(property, result, m, registry, nesting - 1);
                }
                result.append(value, lastPosition, value.length());
            } else {
                if (missed.add(property)) { // avoid log flood
                    logger.warn("Property '{}' for not found in registry while resolving '{}'", property, key);
                }
                result.append(matcher.group());
            }
        }

        @Override
        public String toString() {
            return "Interpolator{}";
        }
    }

    public static class SelectiveInterpolator extends DefaultInterpolator {
        private final ImmutableSet<String> marked;
        private final String marker;
        private final boolean exclude;

        public SelectiveInterpolator(String marker, ImmutableSet<PropertyDefinition> defaults) {
            checkArgument(!Strings.isNullOrEmpty(marker), "Empty marker value not allowed");
            checkArgument(!marker.equals("!"), "No marker provided for exclusion");
            checkArgument(!defaults.isEmpty(), "Default properties values seems to be not loaded yet");

            this.exclude = marker.startsWith("!");
            this.marker = marker;

            if (exclude) {
                marker = marker.substring(1);
            }

            ImmutableSet.Builder<String> marked = ImmutableSet.builder();
            for (PropertyDefinition definition : defaults) {
                String annotation = PropertyModule.getPropertyAnnotation(definition, marker);
                if (annotation != null) {
                    marked.add(definition.getName());
                }
            }

            this.marked = marked.build();
            if (this.marked.isEmpty()) {
                logger.warn("No properties marked with {} found, all interpolation is {}", this.marker, this.exclude? "on": "off");
            }
        }

        @Nonnull
        @Override
        public String interpolate(String key, String value, Map<String, String> registry) {
            if (marked.contains(key) == exclude) {
                return value;
            }
            return super.interpolate(key, value, registry);
        }

        @Override
        void doInterpolation(String key, StringBuilder result, Matcher matcher, Map<String, String> registry, int nesting) {
            String property = matcher.group(1);
            if (marked.contains(property) == exclude) {
                String value = registry.get(property);
                if (value != null) {
                    result.append(value);
                    return;
                }
            }
            super.doInterpolation(key, result, matcher, registry, nesting);
        }

        @Override
        public String toString() {
            return String.format("Interpolator{%s}", marker);
        }
    }

    public static class OverridenPropertyBinding implements InstanceBinding<String> {
        private final Key<String> key;
        private final String value;
        private final Provider<String> provider;
        private final Object source;

        public OverridenPropertyBinding(Binding<String> base, String value) {
            this.key = base.getKey();
            this.value = Preconditions.checkNotNull(value);
            this.provider = Providers.of(value);
            this.source = base.getSource();
        }

        @Override
        public String getInstance() {
            return value;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return ImmutableSet.of();
        }

        @Override
        public Key<String> getKey() {
            return key;
        }

        @Override
        public Provider<String> getProvider() {
            return provider;
        }

        @Override
        public <V> V acceptTargetVisitor(BindingTargetVisitor<? super String, V> visitor) {
            return visitor.visit(this);
        }

        @Override
        public <V> V acceptScopingVisitor(BindingScopingVisitor<V> visitor) {
            return visitor.visitNoScoping();
        }

        @Override
        public Object getSource() {
            return source;
        }

        @Override
        public <T> T acceptVisitor(ElementVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public void applyTo(Binder binder) {
            // instance bindings aren't scoped
            binder.withSource(getSource()).bind(key).toInstance(value);
        }

        @Override
        public Set<Dependency<?>> getDependencies() {
            return ImmutableSet.of();
        }

        @Override public String toString() {
            return Objects.toStringHelper(InstanceBinding.class)
                .add("key", key)
                .add("source", source)
                .add("instance", value)
                .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OverridenPropertyBinding that = (OverridenPropertyBinding) o;
            return key.equals(that.key) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
