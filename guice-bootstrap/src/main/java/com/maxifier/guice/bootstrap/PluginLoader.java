/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.maxifier.guice.bootstrap.InjectorBuilder.loadModule;

/**
 * Loads plugin modules from specified directory.
 *
 * @author Konstantin Lyamshin (2015-11-04 18:08)
 */
public class PluginLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    private static final FileFilter DIRECTORIES = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };
    private static final FileFilter JARS = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isFile() && !file.isHidden() && file.getName().endsWith(".jar");
        }
    };
    private final Map<File, ClassLoader> pluginDirs = new HashMap<File, ClassLoader>();

    /**
     * Create ClassLoader on top of every subdirectory of {@code pluginDir}.
     *
     * @param pluginDir parent directory for individual plugins
     */
    public PluginLoader(File pluginDir) {
        this(pluginDir, false);
    }

    /**
     * Create ClassLoader on top of {@code pluginDir} or its subdirectories.
     *
     * @param pluginDir parent directory for plugins' classpath
     * @param flat true if pluginDir should be used as is without deep into subdirectories
     */
    public PluginLoader(File pluginDir, boolean flat) {
        checkArgument(pluginDir.isDirectory(), "Plugin path is not directory");
        ClassLoader parentCL = this.getClass().getClassLoader();
        if (!flat) {
            for (File dir : pluginDir.listFiles(DIRECTORIES)) {
                pluginDirs.put(dir, initClassLoader(dir, parentCL));
            }
        }
        if (pluginDirs.isEmpty()) { // fall back to a flat directory structure
            pluginDirs.put(pluginDir, initClassLoader(pluginDir, parentCL));
        }
    }

    /**
     * @return parent ClassLoader for plugins' ClassLoaders
     */
    public ClassLoader getClassLoader() {
        if (pluginDirs.isEmpty()) { // should never happen
            return this.getClass().getClassLoader();
        }
        // extract parent class loader from existing childen
        return pluginDirs.values().iterator().next().getParent();
    }

    /**
     * @param parentCL parent ClassLoader for plugins' ClassLoaders
     */
    public void setClassLoader(ClassLoader parentCL) {
        // Reload existing classloaders
        for (Map.Entry<File, ClassLoader> entry : pluginDirs.entrySet()) {
            entry.setValue(initClassLoader(entry.getKey(), parentCL));
        }
    }

    private static ClassLoader initClassLoader(File pluginDir, ClassLoader parentCL) {
        try {
            File[] jars = pluginDir.listFiles(JARS);
            URL[] urls = new URL[jars.length];
            for (int i = 0; i < jars.length; i++) {
                urls[i] = jars[i].toURI().toURL();
            }
            return new URLClassLoader(urls, parentCL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Can't process plugin dir " + pluginDir, e);
        }
    }

    /**
     * Searches plugin ClassLoaders for {@code pluginClassName} and creates plugin using {@code injector}.
     *
     * @param injector configuration injector (see {@link InjectorBuilder#buildConfigurationInjector()})
     * @param pluginClassName plugin module class name
     * @return configured plugin module
     * @throws IllegalArgumentException if plugin not found in classpath
     */
    public Module loadPlugin(Injector injector, String pluginClassName) {
        Module plugin = null;
        for (ClassLoader classLoader : pluginDirs.values()) {
            try {
                Class<?> moduleClass = classLoader.loadClass(pluginClassName);
                if (plugin == null) {
                    plugin = loadModule(injector, moduleClass);
                } else {
                    logger.error("Duplicate plugin found in classpath: " + pluginClassName);
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin module not found in classpath: " + pluginClassName);
        }
        return plugin;
    }
}
