package com.magenta.guice.plugins;

import com.google.inject.AbstractModule;/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 21.01.2010 
* 
* Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Foo.class).to(FooImpl.class);
    }
}
