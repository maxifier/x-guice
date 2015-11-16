/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;

/**
 * @author Konstantin Lyamshin (2015-11-05 20:18)
 */
public class SpecificModule2 extends AbstractModule {
    @Override
    protected void configure() {
        bind(FooImpl.class).to(FooImpl2.class);
    }
}
