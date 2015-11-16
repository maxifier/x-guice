package com.maxifier.guice.lifecycle;

import com.google.inject.*;
import com.google.inject.name.Names;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static org.testng.Assert.*;

/**
 * Project: Maxifier
 * Date: 17.09.2009
 * Time: 15:23:47
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class LifecycleTest {


    @Test
    public void testLifecycle() {
        final Instance instance = new Instance();
        Injector injector = Guice.createInjector(new LifecycleModule(), new AbstractModule() {
            @Override
            protected void configure() {

                bind(Foo1.class).to(Annotated.class);
                bind(Foo2.class).to(NotAnnotated.class).in(Scopes.SINGLETON);
                bind(Foo3.class).to(Eager.class).asEagerSingleton();
                bind(Alone.class).asEagerSingleton();
                bind(WithErrors.class);
                bind(ToInstance.class).toInstance(instance);
                bind(Foo1.class).annotatedWith(Names.named("foo")).toProvider(FooProvider.class);
                bind(Foo1.class).annotatedWith(Names.named("foo-instance")).toProvider(new FooProviderInstance());
            }
        });
        Annotated annotated = (Annotated) injector.getInstance(Foo1.class);
        NotAnnotated notAnnotated = (NotAnnotated) injector.getInstance(Foo2.class);
        Eager eager = (Eager) injector.getInstance(Foo3.class);
        Alone alone = injector.getInstance(Alone.class);
        injector.getInstance(Key.get(Foo1.class, Names.named("foo")));


        WithErrors withErrors = injector.getInstance(WithErrors.class);
        assertTrue(annotated.started);
        assertTrue(notAnnotated.started);
        assertTrue(eager.started);
        assertTrue(alone.started);
        assertTrue(FooProvider.started);
        assertTrue(FooProviderInstance.started);
        assertFalse(instance.finished);
        assertFalse(annotated.finished);
        assertFalse(notAnnotated.finished);
        assertFalse(eager.finished);
        assertFalse(alone.finished);
        assertFalse(FooProvider.finished);
        assertFalse(FooProviderInstance.finished);

        Lifecycle.Errors errors = Lifecycle.destroy(injector);
        assertTrue(annotated.finished);
        assertTrue(notAnnotated.finished);
        assertTrue(eager.finished);
        assertTrue(alone.finished);
        assertTrue(FooProviderInstance.finished);
        assertTrue(FooProvider.finished);
        assertTrue(annotated.started);
        assertTrue(notAnnotated.started);
        assertTrue(FooProvider.started);
        assertTrue(FooProviderInstance.started);
        assertTrue(eager.started);
        assertTrue(alone.started);
        assertTrue(instance.finished);

        assertEquals(errors.getErrorsMap().size(), 1);
        Throwable cause = errors.getErrorsMap().get(withErrors);
        assertNotNull(cause);
        errors.print();
    }


    static interface Foo1 {
    }

    static interface Foo2 {
    }

    static interface Foo3 {
    }

    static interface ToInstance {

    }

    @Singleton
    static class Annotated implements Foo1 {

        boolean started = false;
        boolean finished = false;

        @PostConstruct
        void start() {
            started = true;
        }

        @PreDestroy
        void finish() {
            finished = true;
        }
    }

    static class NotAnnotated implements Foo2 {

        boolean started = false;
        boolean finished = false;

        @PostConstruct
        void start() {
            started = true;
        }

        @PreDestroy
        void finish() {
            finished = true;
        }

    }

    static class Alone {

        boolean started = false;
        boolean finished = false;

        @PostConstruct
        void start() {
            started = true;
        }

        @PreDestroy
        void finish() {
            finished = true;
        }

    }

    static class Eager implements Foo3 {

        boolean started = false;
        boolean finished = false;

        @PostConstruct
        void start() {
            started = true;
        }

        @PreDestroy
        void finish() {
            finished = true;
        }
    }

    @Singleton
    static class WithErrors {

        @PreDestroy
        void finish(Object a) {
        }

    }

    static class FooProvider implements Provider<Foo1> {

        static boolean started = false;
        static boolean finished = false;

        @PostConstruct
        void create() {
            started = true;
        }

        @Override
        public Foo1 get() {
            return null;
        }

        @PreDestroy
        void destroy() {
            finished = true;
        }
    }

    static class FooProviderInstance implements Provider<Foo1> {

        static boolean started = false;
        static boolean finished = false;

        @PostConstruct
        void create() {
            started = true;
        }

        @Override
        public Foo1 get() {
            return null;
        }

        @PreDestroy
        void destroy() {
            finished = true;
        }
    }

    static class Instance implements ToInstance {
        boolean finished = false;

        @PreDestroy
        void finish() {
            finished = true;
        }
    }
}
