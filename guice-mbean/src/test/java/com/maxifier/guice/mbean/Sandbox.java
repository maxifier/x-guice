package com.maxifier.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 10:13 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class Sandbox {

    public static void main(String[] args) throws InterruptedException {
        Injector inj = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(MBeanManagerModule.platform("test"));
                bind(Foo.class).asEagerSingleton();
            }
        });
        Thread.sleep(100000000L);
    }

    @MBean(name = "service=Foo")
    static class Foo {

        @MBeanMethod
        String name() {
            return "Foo";
        }
    }
}
