/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import javax.inject.Inject;

/**
 * @author Konstantin Lyamshin (2015-11-06 12:00)
 */
public class CommonService {
    private final Foo foo;

    @Inject
    public CommonService(Foo foo) {
        this.foo = foo;
    }

    @Override
    public String toString() {
        return foo.name();
    }
}
