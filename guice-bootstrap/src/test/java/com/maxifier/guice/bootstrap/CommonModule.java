/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Konstantin Lyamshin (2015-11-05 20:18)
 */
public class CommonModule extends AbstractModule {
    @Override
    protected void configure() {
        // Binds a service which requires Foo interface
        bind(CommonService.class).in(Scopes.NO_SCOPE);
    }

    @Override
    public String toString() {
        return "CommonModule{}";
    }
}
