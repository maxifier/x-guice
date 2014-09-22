package com.magenta.guice.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * Created by: Aleksey Didik
 * Date: 6/21/11
 * Time: 11:33 AM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ContainerErrorTest {

    @Test
    public void testContainerError() throws Exception {

        try {
            Injector inj = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo.class).asEagerSingleton();
                }
            });
        } catch (Exception e) {
            long f = 5;
        }


    }

    static class Foo {
        @Inject
        Foo(String[] a) {
        }
    }
}
