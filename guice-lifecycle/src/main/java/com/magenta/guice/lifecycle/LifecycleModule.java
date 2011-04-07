package com.magenta.guice.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.annotation.PostConstruct;
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
        AnnotatedMethodCache cache = new AnnotatedMethodCache(PostConstruct.class);
        AnnotatedMethodMatcher methodMatcher = new AnnotatedMethodMatcher(PostConstruct.class);
        LifecycleTypeListener listener = new LifecycleTypeListener(cache);
        bindListener(methodMatcher, listener);
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

    private static class LifecycleTypeListener implements TypeListener {

        private final AnnotatedMethodCache cache;

        public LifecycleTypeListener(AnnotatedMethodCache cache) {
            this.cache = cache;
        }

        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            encounter.register(new InjectionListener<I>() {
                @SuppressWarnings({"ThrowInsideCatchBlockWhichIgnoresCaughtException"})
                @Override
                public void afterInjection(I injectee) {
                    Method[] methods = cache.get(injectee.getClass());
                    for (Method method : methods) {
                        try {
                            method.invoke(injectee);
                        } catch (InvocationTargetException ie) {
                            Throwable e = ie.getTargetException();
                            throw new ProvisionException(e.getMessage(), e);
                        } catch (IllegalAccessException e) {
                            throw new ProvisionException(e.getMessage(), e);
                        }
                    }
                }
            });
        }

    }
}