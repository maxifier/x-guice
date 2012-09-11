package com.magenta.guice.scope;

import static org.junit.Assert.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.junit.Test;

/**
 * Created by: Aleksey Didik
 * Date: 5/17/11
 * Time: 4:29 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class SingletonCheckTest {

    @Test
    public void testSingleton() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Foo.class).to(FooImpl.class);
            }
        });

        Foo foo1 = injector.getInstance(Foo.class);
        Foo foo2 = injector.getInstance(Foo.class);
        assertEquals(foo1, foo2);

    }

    static interface Foo {

    }

    @Singleton
    static class FooImpl implements Foo {

    }
}


