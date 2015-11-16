/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Konstantin Lyamshin (2015-11-06 13:47)
 */
public class Multibinder2 extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Foo.class)
            .addBinding().to(FooImplEx.class);
    }
}
