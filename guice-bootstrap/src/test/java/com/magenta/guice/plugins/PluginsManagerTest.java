package com.magenta.guice.plugins;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.magenta.guice.bootstrap.plugins.PluginsManager;
import org.junit.Test;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;

/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 21.01.2010 
* 
* Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class PluginsManagerTest {

    @Test
    public void testPluginManager() throws Exception {
        Collection<Module> modules = PluginsManager.loadModules(new File("src/test/jars"), null);
        assertEquals(modules.size(), 2);
        Injector inj = Guice.createInjector(new MainModule());

        PluginsManager.loadPlugins(inj, new File("src/test/jars"), null);
    }

    @Test
    public void testPluginManagerWithClassLoader() throws Exception {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Collection<Module> modules = PluginsManager.loadModules(new File("src/test/jars"), systemClassLoader);
        assertEquals(modules.size(), 2);

        Injector inj = Guice.createInjector(new MainModule());
        PluginsManager.loadPlugins(inj, new File("src/test/jars"), systemClassLoader);
    }
}
