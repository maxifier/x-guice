/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Konstantin Lyamshin (2015-11-06 13:43)
 */
public class Multibinder1 extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Foo.class)
            .addBinding().to(FooImpl2.class);
    }
}
