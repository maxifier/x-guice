package com.magenta.guice.jpa;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

import javax.persistence.EntityManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/
public class JPAModule implements Module {

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void configure(Binder builder) {
        DBInterceptor interceptor = new DBInterceptor();
        builder.bind(DBInterceptor.class).toInstance(interceptor);
        builder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(DB.class), interceptor);
        builder.bind(EntityManager.class).toProvider(DBInterceptor.class).in(Scopes.SINGLETON);
        if (builder.currentStage() == Stage.PRODUCTION) {
            //examine used annotations
            builder.bindListener(new AnnotatedMethodMatcher(DB.class), interceptor);
        }
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
