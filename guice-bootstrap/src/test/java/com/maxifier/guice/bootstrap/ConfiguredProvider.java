/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.Module;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Konstantin Lyamshin (2015-11-05 21:17)
 */
public class ConfiguredProvider implements Provider<Module> {
    private final Module module;

    @Inject
    public ConfiguredProvider(Map<String, ModuleBundle> bundles, Set<Module> modules, Properties properties, @Named("injected.cfg") String config) {
        this.module = new ConfiguredModule().set(bundles, modules, properties, config);
    }

    @Override
    public Module get() {
        return module;
    }
}
