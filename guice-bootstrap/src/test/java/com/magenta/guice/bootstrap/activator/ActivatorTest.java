package com.magenta.guice.bootstrap.activator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.Test;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 25.02.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
@Test
public class ActivatorTest {

    @Test
    public void testAllRight() {
        Injector inj = Guice.createInjector(new ActivatorModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimpleActivator.class).asEagerSingleton();
            }
        });
        ActivatorManager am = inj.getInstance(ActivatorManager.class);
        am.activate();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testTooManyParams() {
        Injector inj = Guice.createInjector(new ActivatorModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(TooManyParams.class).asEagerSingleton();
            }
        });
        ActivatorManager am = inj.getInstance(ActivatorManager.class);
        am.activate();
    }

    @Test
    public void testPrivate() {
        Injector inj = Guice.createInjector(new ActivatorModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(PrivateActivator.class).asEagerSingleton();
            }
        });
        ActivatorManager am = inj.getInstance(ActivatorManager.class);
        am.activate();
    }


    private static class SimpleActivator {
        @Activate
        public void activate() {
            System.out.println("activator activated");
        }
    }

    private static class TooManyParams {
        @Activate
        public void activate(int a, int b) {
            System.out.println("activator activated");
        }
    }


    private static class PrivateActivator {
        @Activate
        private void activate() {
            System.out.println("activator activated");
        }
    }
}
