/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;

/**
 * @author Konstantin Lyamshin (2015-11-06 12:22)
 */
public class PluginModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Foo.class).to(FooImplEx.class);
    }
}
