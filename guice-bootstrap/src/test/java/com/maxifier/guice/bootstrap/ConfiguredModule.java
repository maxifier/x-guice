/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Konstantin Lyamshin (2015-11-05 21:11)
 */
public class ConfiguredModule extends AbstractModule {
    private String config = "null";

    @Inject
    public ConfiguredModule set(Map<String, ModuleBundle> bundles, Set<Module> modules, Properties properties, @Named("injected.cfg") String config) {
        this.config = String.format("b=%s,m=%s,p=%s,c=%s", bundles.keySet(), modules, properties.getProperty("custom.cfg"), config);
        return this;
    }

    @Override
    protected void configure() {
        bind(List.class).to(ArrayList.class);
        bindConstant().annotatedWith(Names.named("config")).to(config);
    }
}
