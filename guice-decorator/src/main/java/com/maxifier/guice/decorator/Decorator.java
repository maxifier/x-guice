package com.maxifier.guice.decorator;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.util.Providers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;

/**
 * Project: Maxifier
 * Date: 09.11.2009
 * Time: 11:44:23
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class Decorator {

    private final Binder binder;
    private Injector inj;
    private final Collection<Class<?>> members = new HashSet<Class<?>>(4);

    public Decorator(Binder binder) {
        this.binder = binder;
        binder.requestInjection(new InjectorCatcher());
        binder.requestInjection(new Cleaner());
    }

    public <T> DecoratorProviderBuilder<T> bind(Class<T> clazz) {
        members.add(clazz);
        return new DecoratorProviderBuilder<T>(clazz);
    }

    public class DecoratorProviderBuilder<T> {

        private final Class<T> binded;
        private Class<? extends Annotation> annotation;
        private boolean eager = false;
        private Scope inScope;

        public DecoratorProviderBuilder(Class<T> clazz) {
            binded = clazz;
        }

        public DecoratorProviderBuilder<T> annotatedWith(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        public DecoratorProviderBuilder<T> in(Scope scope) {
            this.inScope = scope;
            return this;
        }

        public DecoratorProviderBuilder<T> asEagerSingleton() {
            this.eager = true;
            return this;
        }

        public <S extends T> DecoratorProvider<T> to(Class<S> clazz) {
            checkAndAddMember(clazz, binded);
            final DecoratorProvider<T> provider = new DecoratorProvider<T>(binded, clazz);
            if (annotation == null) {
                if (eager) {
                    binder.bind(binded).toProvider(provider).asEagerSingleton();
                } else if (inScope != null) {
                    binder.bind(binded).toProvider(provider).in(this.inScope);
                } else {
                    binder.bind(binded).toProvider(provider);
                }
            } else {
                if (eager) {
                    binder.bind(binded).annotatedWith(annotation).toProvider(provider).asEagerSingleton();
                } else if (inScope != null) {
                    binder.bind(binded).annotatedWith(annotation).toProvider(provider).in(this.inScope);
                } else {
                    binder.bind(binded).annotatedWith(annotation).toProvider(provider);
                }
            }
            return provider;
        }

        @Override
        public String toString() {
            return "DecoratorProviderBuilder{" +
                    "binded=" + binded +
                    ", annotation=" + annotation +
                    '}';
        }
    }

    private void checkAndAddMember(Class<?> clazz, Class<?> binded) {
        if (members.contains(clazz)) {
            throw new DuplicateMemberException(binded, clazz);
        }
        members.add(clazz);
    }

    public class DecoratorProvider<T> implements Provider<T> {

        private final Class<T> binded;
        private final Class<? extends T> impl;
        private Provider<T> next;
        private Injector child;

        public DecoratorProvider(Class<T> binded, Class<? extends T> impl) {
            this.binded = binded;
            this.impl = impl;
        }

        @Override
        public T get() {
            if (next == null) {
                return inj.getInstance(impl);
            }
            if (child == null) {
                Module module = new AbstractModule() {
                    @Override
                    protected void configure() {
                        this.bind(binded).annotatedWith(Decorated.class).toProvider(next);
                    }
                };
                child = inj.createChildInjector(module);
            }
            return child.getInstance(impl);
        }

        public DecoratorProvider<T> decorate(Class<? extends T> clazz) {
            checkAndAddMember(clazz, binded);
            final DecoratorProvider<T> nextProvider = new DecoratorProvider<T>(binded, clazz);
            next = nextProvider;
            return nextProvider;
        }

        public void decorate(T instance) {
            checkAndAddMember(instance.getClass(), binded);
            next = Providers.of(instance);
        }

        @Override
        public String toString() {
            return "decorate " + impl;
        }
    }

    private class InjectorCatcher {
        @Inject
        public void catchInjector(Injector catched) {
            inj = catched;
        }
    }

    private class Cleaner {
        @Inject
        public void clear() {
            members.clear();
        }
    }

    @Override
    public String toString() {
        return "Decorator{" +
                "members=" + members +
                '}';
    }
}
