package com.maxifier.guice.bootstrap.xml;

import com.google.inject.AbstractModule;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 18:24:06
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class FooModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Foo.class).to(FooImpl.class);
    }
}
