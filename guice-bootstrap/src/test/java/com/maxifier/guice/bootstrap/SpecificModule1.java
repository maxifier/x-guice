/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;

/**
 * @author Konstantin Lyamshin (2015-11-05 20:18)
 */
public class SpecificModule1 extends AbstractModule {
    @Override
    protected void configure() {
        bind(Foo.class).to(FooImpl.class);
    }

    @Override
    public String toString() {
        return "SpecificModule1{}";
    }
}
