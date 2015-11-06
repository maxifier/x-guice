/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;

/**
 * @author Konstantin Lyamshin (2015-11-05 22:02)
 */
public class SpecificModule3 extends AbstractModule {
    @Override
    protected void configure() {
        install(Modules.override(new SpecificModule1()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Foo.class).to(FooImplEx.class);
            }
        }));
    }
}
