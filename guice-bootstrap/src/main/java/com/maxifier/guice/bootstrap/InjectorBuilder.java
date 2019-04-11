/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.common.base.Splitter;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Builds main application injector using set of modules and plugins.
 * <p>Look at individual method's descriptions.</p>
 *
 * @author Konstantin Lyamshin (2015-11-04 17:12)
 */
public class InjectorBuilder {
    private static final Logger logger = LoggerFactory.getLogger(InjectorBuilder.class);
    private final HashMap<String, ModuleBundle> bundles = new HashMap<String, ModuleBundle>();
    private final HashSet<Module> modules = new HashSet<Module>();
    private final HashSet<Module> plugins = new HashSet<Module>();
    private Properties configuration = new Properties();
    private PluginLoader pluginLoader;
    private ClassLoader classLoader;

    /**
     * Sets configuration properties
     */
    public InjectorBuilder withConfiguration(Properties configuration) {
        this.configuration = checkNotNull(configuration, "Configuration not specified");
        return this;
    }

    /**
     * Sets directory for plugin loading
     *
     * @see PluginLoader#PluginLoader(File)
     */
    public InjectorBuilder withPluginDir(File pluginDir) {
        this.pluginLoader = new PluginLoader(pluginDir);
        if (classLoader != null) {
            pluginLoader.setClassLoader(classLoader);
        }
        return this;
    }

    /**
     * Sets directory for plugin loading
     *
     * @see PluginLoader#PluginLoader(File, boolean)
     */
    public InjectorBuilder withPluginDir(File pluginDir, boolean flat) {
        this.pluginLoader = new PluginLoader(pluginDir, flat);
        if (classLoader != null) {
            pluginLoader.setClassLoader(classLoader);
        }
        return this;
    }

    /**
     * Sets classloader for module classes and parent for plugin loaders
     *
     * @see PluginLoader#setClassLoader(ClassLoader)
     */
    public InjectorBuilder withClassLoader(ClassLoader classLoader) {
        this.classLoader = checkNotNull(classLoader, "ClassLoader not specified");
        if (pluginLoader != null) {
            pluginLoader.setClassLoader(classLoader);
        }
        return this;
    }

    /**
     * Appends application module
     */
    public InjectorBuilder withModule(Module module) {
        if (!modules.add(checkNotNull(module, "Module not specified"))) {
            logger.error("Skipping duplicated module {} with class {}", module, module.getClass().getName());
        }
        return this;
    }

    /**
     * Reads module bundle from configuration by name and appends it to application modules
     */
    public InjectorBuilder withModuleBundle(String bundleName) {
        return withModuleBundle(toBundle(bundleName));
    }

    /**
     * Reads module bundle from configuration by full enum name and appends it to application modules
     */
    public InjectorBuilder withModuleBundle(Enum<?> bundleEnum) {
        return withModuleBundle(toBundle(toName(bundleEnum)));
    }

    /**
     * Loads bundle modules and appends them to application modules
     * @see ModuleBundle Module bootstrapping process
     * @see #buildConfigurationInjector() Instantiation injector
     */
    public InjectorBuilder withModuleBundle(ModuleBundle bundle) {
        if (bundles.containsKey(bundle.name())) {
            logger.error("Skipping duplicated bundle {} with class {}", bundle.name(), bundle.getClass().getName());
            return this;
        }

        ClassLoader classLoader = this.classLoader != null? this.classLoader: this.getClass().getClassLoader();

        Injector injector = buildConfigurationInjector();
        ArrayList<Module> modules = new ArrayList<Module>();
        for (String moduleName : bundle.modules()) {
            try {
                Class<?> moduleClass = classLoader.loadClass(moduleName);
                Module module = loadModule(injector, moduleClass);
                if (module != Modules.EMPTY_MODULE) {
                    modules.add(module);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Can't load module class", e);
            }
        }

        bundles.put(bundle.name(), bundle);
        for (Module module : modules) {
            withModule(module);
        }

        return this;
    }

    /**
     * Appends plugin module
     */
    public InjectorBuilder withPlugin(Module module) {
        if (!plugins.add(checkNotNull(module, "Module not specified"))) {
            logger.error("Skipping duplicated plugin {} with class {}", module, module.getClass().getName());
        }
        return this;
    }

    /**
     * Reads module bundle from configuration by name and appends it to plugins
     */
    public InjectorBuilder withPluginBundle(String bundleName) {
        return withPluginBundle(toBundle(bundleName));
    }

    /**
     * Reads module bundle from configuration by full enum name and appends it to plugins
     */
    public InjectorBuilder withPluginBundle(Enum<?> bundleEnum) {
        return withPluginBundle(toBundle(toName(bundleEnum)));
    }

    /**
     * Loads bundle modules and appends them to plugin
     * <p>Plugin modules loaded in a child ClassLoaders. To build them you must initialize
     * class path using {@link #withPluginDir(File)}.</p>
     *
     * @see PluginLoader#loadPlugin(Injector, String)
     * @see ModuleBundle Module bootstrapping process
     * @see #buildConfigurationInjector() Instantiation injector
     */
    public InjectorBuilder withPluginBundle(ModuleBundle bundle) {
        if (pluginLoader == null) {
            throw new IllegalStateException("Plugin directory not specified");
        }

        Injector injector = buildConfigurationInjector();
        ArrayList<Module> plugins = new ArrayList<Module>();
        for (String moduleName : bundle.modules()) {
            Module plugin = pluginLoader.loadPlugin(injector, moduleName);
            if (plugin != Modules.EMPTY_MODULE) {
                plugins.add(plugin);
            }
        }

        for (Module plugin : plugins) {
            withPlugin(plugin);
        }

        return this;
    }

    /**
     * Builds special configuration injector which is used for instantiation modules and plugins.
     * <p>Injector contains:</p>
     * <ul>
     * <li>{@code Properties} binding for current configuration.</li>
     * <li>{@code @Named String} bindings for individual parameters.</li>
     * <li>{@code Map&lt;String, ModuleBundle&gt;} for current module bundles.</li>
     * <li>{@code Set&lt;Module&gt;} for current application modules.</li>
     * </ul>
     */
    public Injector buildConfigurationInjector() {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                Names.bindProperties(binder(), configuration);
                bind(Properties.class).toInstance(new Properties(configuration)); // use default properties to protect configuration from changes
                bind(new TypeLiteral<Map<String, ModuleBundle>>(){}).toInstance(unmodifiableMap(bundles));
                bind(new TypeLiteral<Set<Module>>(){}).toInstance(unmodifiableSet(modules));
            }
        });
    }

    /**
     * Builds combined application module.
     * <p>Returned module represent all modules registered by this object overridden by registered plugins.</p>
     */
    public Module buildApplicationModule() {
        return plugins.isEmpty()
            ? Modules.combine(modules) // No overrides for more clear guice error messages
            : Modules.override(modules).with(plugins);
    }

    /**
     * Builds combined application module and creates injector using it.
     */
    public Injector buildApplicationInjector(Stage stage) {
        for (String bundle : bundles.keySet()) {
            logger.debug("Installing {} module bundle", bundle);
        }
        for (Module plugin : plugins) {
            logger.debug("Installing {} plugin", plugin);
        }
        return Guice.createInjector(stage, buildApplicationModule());
    }

    private static String toName(Enum<?> bundleEnum) {
        return bundleEnum.getDeclaringClass().getName() + '.' + bundleEnum.name();
    }

    private ModuleBundle toBundle(final String bundleName) {
        String bundle = configuration.getProperty(bundleName);
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle " + bundleName + " not found in configuration");
        }
        final Iterable<String> modules = Splitter.on(',')
            .trimResults()
            .omitEmptyStrings()
            .split(bundle);

        return new ModuleBundle() {
            @Override
            public String name() {
                return bundleName;
            }

            @Override
            public Iterable<String> modules() {
                return modules;
            }

            @Override
            public String toString() {
                return String.format("ModuleBundle{%s}", bundleName);
            }
        };
    }

    static Module loadModule(Injector injector, Class<?> moduleClass) {
        try {
            Object module = injector.getInstance(moduleClass);
            if (module instanceof Provider) {
                module = ((Provider) module).get();
            }
            if (module instanceof Module) {
                return (Module) module;
            }
            throw new IllegalArgumentException("Class doesn't implement Module or Provider<Module>: " + moduleClass.getName());
        } catch (ConfigurationException e) {
            throw new IllegalArgumentException("Can't load module class", e);
        } catch (ProvisionException e) {
            throw new IllegalArgumentException("Can't load module class", e);
        }
    }
}
