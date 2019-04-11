package com.maxifier.guice.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 17:20:37
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class LifecycleModule extends AbstractModule {

    public static void bind(Binder binder) {
        binder.install(new LifecycleModule());
    }


    @Override
    protected void configure() {
        //@PostConstruct
        bindListener(
                new AnnotatedMethodMatcher(PostConstruct.class),
                new PostConstructTypeListener(PostConstruct.class)
        );
        //@ShutdownSafe
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(ShutdownSafe.class), new SafeShutdownInterceptor());
        //Destroy container during JVM shutdown
        bind(ShutdownHook.class).asEagerSingleton();
    }

    static class AnnotatedMethodMatcher extends AbstractMatcher<TypeLiteral<?>> {

        private final Class<? extends Annotation>[] annotations;

        public AnnotatedMethodMatcher(Class<? extends Annotation>... annotations) {
            this.annotations = annotations;
        }

        @Override
        public boolean matches(TypeLiteral<?> typeLiteral) {
            final Class<?> type = typeLiteral.getRawType();
            return isMatches(type);
        }

        private boolean isMatches(Class<?> type) {
            while (true) {
                if (type.equals(Object.class)) {
                    return false;
                }
                for (Method method : type.getDeclaredMethods()) {
                    for (Class<? extends Annotation> a : annotations) {
                        if (method.isAnnotationPresent(a)) {
                            return true;
                        }
                    }
                }
                type = type.getSuperclass();
            }
        }

        @Override
        public String toString() {
            return "AnnotatedMethodMatcher{" +
                    "annotations=" + (annotations == null ? null : Arrays.asList(annotations)) +
                    '}';
        }

    }

    private static class PostConstructTypeListener implements TypeListener {

        private final AnnotatedMethodCache cache;

        public PostConstructTypeListener(Class<? extends Annotation>... annotationClasses) {
            this.cache = new AnnotatedMethodCache(annotationClasses);
        }

        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            encounter.register(new InjectionListener<I>() {
                @Override
                public void afterInjection(I injectee) {
                    Method[] methods = cache.get(injectee.getClass());
                    for (Method method : methods) {
                        try {
                            method.invoke(injectee);
                        } catch (InvocationTargetException ie) {
                            String message = ie.getTargetException().getMessage();
                            throw new ProvisionException("Exception in @PostConstruct: " + message, ie.getTargetException());
                        } catch (IllegalAccessException e) {
                            throw new ProvisionException("Exception in @PostConstruct: " + e.getMessage(), e);
                        }
                    }
                }
            });
        }

    }

    static class ShutdownHook {

        @Inject
        void register(final Injector injector) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    Lifecycle.destroy(injector);
                }
            });
        }
    }
}