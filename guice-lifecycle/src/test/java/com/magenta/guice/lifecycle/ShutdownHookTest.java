package com.magenta.guice.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import javax.annotation.PreDestroy;

/**
 * Created by: Aleksey Didik
 * Date: 6/10/11
 * Time: 5:31 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ShutdownHookTest {

    public static void main(String[] args) {
        Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new LifecycleModule());
                bind(Foo.class).asEagerSingleton();
            }
        });
        System.exit(143);
    }

    static class Foo {

        @PreDestroy
        void hello() {
            System.out.println("Bye @PreDestroy");
        }
    }
}

