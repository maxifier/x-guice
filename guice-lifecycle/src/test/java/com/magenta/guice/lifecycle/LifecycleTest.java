package com.magenta.guice.lifecycle;

import com.google.inject.*;
import com.magenta.guice.lifecycle.Lifecycle;
import com.magenta.guice.lifecycle.LifecycleModule;
import static org.testng.Assert.*;
import org.testng.annotations.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
            }
        });
        Annotated annotated = (Annotated) injector.getInstance(Foo1.class);
        NotAnnotated notAnnotated = (NotAnnotated) injector.getInstance(Foo2.class);
        Eager eager = (Eager) injector.getInstance(Foo3.class);
        Alone alone = injector.getInstance(Alone.class);

        WithErrors withErrors = injector.getInstance(WithErrors.class);
        assertTrue(annotated.started);
        assertTrue(notAnnotated.started);
        assertTrue(eager.started);
        assertTrue(alone.started);
        assertFalse(instance.finished);
        assertFalse(annotated.finished);
        assertFalse(notAnnotated.finished);
        assertFalse(eager.finished);
        assertFalse(alone.finished);

        Lifecycle.Errors errors = Lifecycle.destroy(injector);
        assertTrue(annotated.finished);
        assertTrue(notAnnotated.finished);
        assertTrue(eager.finished);
        assertTrue(alone.finished);
        assertTrue(annotated.started);
        assertTrue(notAnnotated.started);
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

    static class Instance implements ToInstance {
        boolean finished = false;

        @PreDestroy
        void finish() {
            finished = true;
        }
    }
}
