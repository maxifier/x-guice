package com.magenta.guice.bootstrap.plugins;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.magenta.guice.bootstrap.model.Plugin;
import com.magenta.guice.bootstrap.model.io.xpp3.XGuicePluginXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;

/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public final class PluginsManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginsManager.class);

    private static final String PLUGIN_INFO_PATH = "META-INF/plugin.xml";

    private static final FilenameFilter jarFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };


    public static Injector loadPlugins(Injector injector, File pluginsPath) {
        checkPath(pluginsPath);
        URL[] jars = scanJars(pluginsPath);
        ClassLoader baseInjCL = injector.getClass().getClassLoader();
        ClassLoader pluginsCL = new URLClassLoader(jars, baseInjCL);
        Collection<Plugin> plugins = scan(pluginsPath);
        for (Plugin plugin : plugins) {
            String moduleName = plugin.getModule();
            try {
                Module module = (Module) pluginsCL.loadClass(moduleName).newInstance();
                injector = injector.createChildInjector(module);
                logger.info("Plugin {} has been loaded.", plugin.getName());
            } catch (InstantiationException e) {
                logger.warn("Unable to instantiate module " + moduleName + " of plugin " + plugin.getName(), e);
            } catch (IllegalAccessException e) {
                logger.warn("Unable to instantiate module " + moduleName + " of plugin " + plugin.getName(), e);
            } catch (ClassNotFoundException e) {
                logger.warn("Module class " + moduleName + " of plugin " + plugin.getName() + " is not found into classpath.", e);
            }
        }
        return injector;
    }

    private static URL[] scanJars(File pluginsPath) {
        //prepare jars URL
        File[] jarFiles = pluginsPath.listFiles(jarFilter);
        URL[] urls = new URL[jarFiles.length];
        int i = 0;
        for (File jarFile : jarFiles) {
            try {
                urls[i++] = jarFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Something wrong in filesystem" +
                        " if available file path can't converted to URL", e);
            }
        }
        return urls;
    }

    static Collection<Plugin> scan(File pluginsPath) {
        File[] jarFiles = pluginsPath.listFiles(jarFilter);
        Collection<Plugin> plugins = new HashSet<Plugin>();
        XGuicePluginXpp3Reader reader = new XGuicePluginXpp3Reader();
        for (File jarFile : jarFiles) {
            try {
                URL url = jarFile.toURI().toURL();
                ClassLoader jarClassloader = new URLClassLoader(new URL[]{url});
                InputStream pluginXmlStream = jarClassloader.getResourceAsStream(PLUGIN_INFO_PATH);
                if (pluginXmlStream != null) {
                    Plugin plugin = reader.read(pluginXmlStream);
                    plugins.add(plugin);
                }
            } catch (MalformedURLException e) {
                logger.warn(String.format("Jar file URL '%s' is invalid", jarFile.toURI()), e);
            } catch (XmlPullParserException e) {
                logger.warn(String.format("plugin.xml of %s is not valid", jarFile.toURI()), e);
            } catch (IOException e) {
                logger.warn(String.format("plugin.xml of %s is not valid", jarFile.toURI()), e);
            }
        }
        return plugins;
    }

    static void checkPath(File path) {
        if (!path.exists()) {
            throw new IllegalArgumentException(String.format("Path '%s' is not exists", path));
        }
        if (path.isFile()) {
            throw new IllegalArgumentException(String.format("Path '%s' must be a directory", path));
        }
    }

    private PluginsManager() {
    }

}

