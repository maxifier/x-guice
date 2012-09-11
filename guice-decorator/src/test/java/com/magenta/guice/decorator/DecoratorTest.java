package com.magenta.guice.decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.spi.RecordingBinder;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Project: Maxifier
 * Date: 09.11.2009
 * Time: 11:45:15
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class DecoratorTest {

    @Test
    public void testFullWrapper() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {

                bind(Service.class).to(GoogleService.class);

                new Decorator(binder())
                        .bind(HelloService.class)
                        .to(WowDecorator.class)
                        .decorate(ServiceDecorator.class)
                        .decorate(HelloRealization.class);
            }
        };

        Injector injector = Guice.createInjector(module);
        final HelloService helloService = injector.getInstance(HelloService.class);
        final String testResult = helloService.hello();
        final String waited = HelloRealization.HELLO + GoogleService.NAME + WowDecorator.WOW;
        assertEquals(testResult, waited);
    }

    @Test
    public void testWithoutDecoration() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                new Decorator(binder())
                        .bind(HelloService.class)
                        .to(HelloRealization.class);
            }
        };

        Injector injector = Guice.createInjector(module);
        final HelloService helloService = injector.getInstance(HelloService.class);
        final String testResult = helloService.hello();
        final String waited = HelloRealization.HELLO;
        assertEquals(testResult, waited);
    }

    @Test(expected = DuplicateMemberException.class)
    public void testEqualBindAndTo() {
        new Decorator(new RecordingBinder(Stage.TOOL))
                .bind(HelloService.class)
                .to(HelloService.class);

    }

    @Test(expected = DuplicateMemberException.class)
    public void testEqualToAndDecorated() {
        new Decorator(new RecordingBinder(Stage.TOOL))
                .bind(HelloService.class)
                .to(HelloRealization.class)
                .decorate(ServiceDecorator.class)
                .decorate(HelloRealization.class);


    }

    @Test
    public void testWithAnnotation() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {

                bind(Service.class).to(GoogleService.class);

                new Decorator(binder())
                        .bind(HelloService.class)
                        .annotatedWith(TestAnnotation.class)
                        .to(WowDecorator.class)
                        .decorate(ServiceDecorator.class)
                        .decorate(HelloRealization.class);
            }
        };

        Injector inj = Guice.createInjector(module);
        final HelloService helloService = inj.getInstance(Key.get(HelloService.class, TestAnnotation.class));
        final String testResult = helloService.hello();
        final String waited = HelloRealization.HELLO + GoogleService.NAME + WowDecorator.WOW;
        assertEquals(testResult, waited);
    }

    @Test
    public void testDecorateInstance() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {

                bind(Service.class).to(GoogleService.class);

                new Decorator(binder())
                        .bind(HelloService.class)
                        .annotatedWith(TestAnnotation.class)
                        .to(WowDecorator.class)
                        .decorate(ServiceDecorator.class)
                        .decorate(new HelloRealization());
            }
        };

        Injector inj = Guice.createInjector(module);
        final HelloService helloService = inj.getInstance(Key.get(HelloService.class, TestAnnotation.class));
        final String testResult = helloService.hello();
        final String waited = HelloRealization.HELLO + GoogleService.NAME + WowDecorator.WOW;
        assertEquals(testResult, waited);
    }

    static boolean eagerFlag = false;

    @Test
    public void testEagerSingleton() {
        eagerFlag = false;
        Module module = new AbstractModule() {
            @Override
            protected void configure() {

                bind(Service.class).to(GoogleService.class);

                new Decorator(binder())
                        .bind(HelloService.class)
                        .asEagerSingleton()
                        .to(EagerService.class);
            }
        };

        Guice.createInjector(module);
        assertTrue(eagerFlag);
        eagerFlag = false;
    }

    static class EagerService implements HelloService {

        EagerService() {
            DecoratorTest.eagerFlag = true;
        }

        @Override
        public String hello() {
            return "eager";
        }
    }


    @Test
    public void testSingletonScope() {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {

                bind(Service.class).to(GoogleService.class);

                new Decorator(binder())
                        .bind(HelloService.class)
                        .in(Scopes.SINGLETON)
                        .to(SingletonService.class);
            }
        };

        Injector inj = Guice.createInjector(module);
        HelloService hs1 = inj.getInstance(HelloService.class);
        HelloService hs2 = inj.getInstance(HelloService.class);
        assertEquals(hs1, hs2);
    }

    static class SingletonService implements HelloService {

        @Override
        public String hello() {
            return "singleton";
        }
    }


    interface Service {
        String serviceName();
    }

    static class GoogleService implements Service {

        public static final String NAME = "Google";


        public String serviceName() {
            return NAME;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
    @BindingAnnotation
    @interface TestAnnotation {
    }

    static class HelloRealization implements HelloService {

        public static final String HELLO = "Hello ";

        public String hello() {
            return HELLO;
        }
    }

    static interface HelloService {

        String hello();
    }

    static class ServiceDecorator implements HelloService {

        private HelloService decorated;
        private final Service justService;

        @Inject
        public ServiceDecorator(@Decorated HelloService decorated, Service justService) {
            this.decorated = decorated;
            this.justService = justService;
        }

        public String hello() {
            return decorated.hello() + justService.serviceName();
        }
    }

    static class WowDecorator implements HelloService {

        private HelloService decorated;
        public static final String WOW = "!";

        @Inject
        public WowDecorator(@Decorated HelloService decorated) {
            this.decorated = decorated;
        }

        public String hello() {
            return decorated.hello() + WOW;
        }
    }
}

