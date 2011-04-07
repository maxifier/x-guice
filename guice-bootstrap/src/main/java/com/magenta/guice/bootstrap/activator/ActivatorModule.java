package com.magenta.guice.bootstrap.activator;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 25.02.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
public class ActivatorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ActivatorManager.class).to(ActivatorManagerImpl.class);
        ActivatorTypeListener listener = new ActivatorTypeListener();
        requestInjection(listener);
        bindListener(new AnnotatedMethodMatcher(Activate.class), listener);
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

}
