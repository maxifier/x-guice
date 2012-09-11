package com.magenta.guice.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

import java.util.Random;

/**
 * Created by: Aleksey Didik
 * Date: 6/7/11
 * Time: 8:34 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class SafeShutdownTest {

    @Test
    public void testSafeShutdown() throws Exception {

    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new LifecycleModule());
                bind(Foo.class);
            }
        });
        new FooThread(1, injector).start();
        new FooThread(2, injector).start();
        new FooThread(3, injector).start();
        new FooThread(4, injector).start();
        System.exit(143);
    }

    static class Foo {

        @ShutdownSafe
        void hello(int a) {
            try {
                Thread.sleep(1000 * 60 * (new Random().nextInt(3) + 1));
            } catch (InterruptedException ignored) {
            }
            System.out.println(a);
        }
    }

    static class FooThread extends Thread {

        private final int a;
        private final Injector inj;

        FooThread(int a, Injector inj) {
            this.a = a;
            this.inj = inj;
        }

        @Override
        public void run() {
            inj.getInstance(Foo.class).hello(a);
        }
    }
}
