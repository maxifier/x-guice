package com.magenta.guice.override;

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.*;
import com.google.inject.util.Modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 08.04.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
public abstract class OverrideModule extends AbstractModule {


    public static Module collect(Module... modules) {
        return collect(Arrays.asList(modules));
    }

    public static Module collect(Iterable<Module> modules) {
        OverrideBinder ovBinder = new OverrideBinder();
        for (Module module : modules) {
            module.configure(ovBinder);
        }
        Module standarts = ovBinder.getStandardModule();
        Module overrides = ovBinder.getOverrideModule();
        return Modules.override(standarts).with(overrides);
    }


    protected <T> LinkedBindingBuilder<T> override(Key<T> key) {
        OverrideBinder ovBinder = getOverrideBinder();
        return ovBinder.override(key);
    }

    protected <T> AnnotatedBindingBuilder<T> override(TypeLiteral<T> typeLiteral) {
        return getOverrideBinder().override(typeLiteral);
    }

    protected <T> AnnotatedBindingBuilder<T> override(Class<T> clazz) {
        return getOverrideBinder().override(clazz);
    }

    protected void override(Module module) {
        getOverrideBinder().override(module);
    }

    public AnnotatedConstantBindingBuilder overrideConstant() {
        return getOverrideBinder().overrideConstant();
    }

    private OverrideBinder getOverrideBinder() {
        Binder binder = binder();
        if (binder instanceof OverrideBinder) {
            return (OverrideBinder) binder;
        } else {
            throw new IllegalStateException("Unable to use override possibility without using OverrideModule.collect(...)");
        }
    }


    public static class OverrideBinder implements Binder {

        private RecordingBinder standard = new RecordingBinder(Stage.TOOL);
        private RecordingBinder override = new RecordingBinder(Stage.TOOL);

        public Module getStandardModule() {
            return Elements.getModule(standard.getElements());
        }

        public Module getOverrideModule() {
            return Elements.getModule(override.getElements());
        }

        public void bindScope(Class<? extends Annotation> scopeAnnotation, Scope scope) {
            standard.bindScope(scopeAnnotation, scope);
        }


        public <T> LinkedBindingBuilder<T> bind(Key<T> key) {
            return standard.bind(key);
        }


        public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
            return standard.bind(typeLiteral);
        }


        public <T> AnnotatedBindingBuilder<T> bind(Class<T> clazz) {
            return standard.bind(clazz);
        }


        public AnnotatedConstantBindingBuilder bindConstant() {
            return standard.bindConstant();
        }

        @Override
        public <T> void requestInjection(TypeLiteral<T> type, T instance) {
            standard.requestInjection(type, instance);
        }

        public void install(Module module) {
            if (module instanceof PrivateModule) {
                standard.install(module);
            } else {
                module.configure(this);
            }
        }

        public void addError(String message, Object... arguments) {
            standard.addError(message, arguments);
        }

        public void addError(Throwable t) {
            standard.addError(t);
        }

        public void addError(Message message) {
            standard.addError(message);
        }

        public void requestInjection(Object instance) {
            standard.requestInjection(instance);
        }

        public void requestStaticInjection(Class<?>... types) {
            standard.requestStaticInjection(types);
        }

        public void bindInterceptor(Matcher<? super Class<?>> classMatcher,
                                    Matcher<? super Method> methodMatcher,
                                    org.aopalliance.intercept.MethodInterceptor... interceptors) {
            standard.bindInterceptor(classMatcher, methodMatcher, interceptors);
        }

        public void requireBinding(Class<?> type) {
            standard.getProvider(type);
        }

        public <T> Provider<T> getProvider(Key<T> key) {
            return standard.getProvider(key);
        }

        public <T> Provider<T> getProvider(Class<T> type) {
            return standard.getProvider(type);
        }

        public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher,
                                   TypeConverter converter) {
            standard.convertToTypes(typeMatcher, converter);
        }

        public Stage currentStage() {
            return standard.currentStage();
        }

        public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
            return standard.getMembersInjector(type);
        }

        public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> type) {
            return standard.getMembersInjector(type);
        }

        public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher,
                                 TypeListener listener) {
            standard.bindListener(typeMatcher, listener);
        }

        @Override
        public Binder withSource(Object source) {
            return standard.withSource(source);
        }

        @Override
        public Binder skipSources(Class... classesToSkip) {
            return standard.skipSources(classesToSkip);
        }

        @Override
        public PrivateBinder newPrivateBinder() {
            return standard.newPrivateBinder();
        }

        @Override
        public void requireExplicitBindings() {
            standard.requireExplicitBindings();
        }

        @Override
        public void disableCircularProxies() {
            standard.disableCircularProxies();
        }


        public <T> LinkedBindingBuilder<T> override(Key<T> key) {
            return override.bind(key);
        }

        public <T> AnnotatedBindingBuilder<T> override(TypeLiteral<T> typeLiteral) {
            return override.bind(typeLiteral);
        }

        public <T> AnnotatedBindingBuilder<T> override(Class<T> clazz) {
            return override.bind(clazz);
        }

        public void override(Module module) {
            override.install(module);
        }

        public AnnotatedConstantBindingBuilder overrideConstant() {
            return override.bindConstant();
        }
    }
}
