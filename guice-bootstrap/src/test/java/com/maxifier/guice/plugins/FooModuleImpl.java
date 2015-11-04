/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.plugins;

import com.google.inject.AbstractModule;

/**
 * FooModuleImpl
 *
 * @author Denis Kokin (denis.kokin@maxifier.com) (2015-05-29 11:39)
 */
public class FooModuleImpl extends AbstractModule implements Foo {
    @Override
    public String getName() {
        return "Guice";
    }

    @Override
    protected void configure() {

    }
}
