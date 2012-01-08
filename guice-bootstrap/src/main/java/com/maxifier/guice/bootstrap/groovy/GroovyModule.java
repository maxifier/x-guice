package com.maxifier.guice.bootstrap.groovy;

import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;
import com.google.inject.spi.TypeListener;
import com.magenta.guice.override.OverrideModule;
import com.magenta.guice.property.PropertyModule;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.aopalliance.intercept.MethodInterceptor;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;


public class GroovyModule extends OverrideModule {

    private final InputStream groovy;
    private final GroovyShell groovyShell;

    public GroovyModule(InputStream groovy) {
        this.groovy = groovy;
        this.groovyShell = new GroovyShell(this.getClass().getClassLoader());
    }

    public GroovyModule(InputStream groovy, GroovyShell groovyShell) {
        this.groovy = groovy;
        this.groovyShell = groovyShell;
    }


    @Override
    protected void configure() {
        //all to mix classes
        ExpandoMetaClass.enableGlobally();
        //who is binder?
        groovyShell.setProperty("binder", binder());
        //get script from stream
        InputStreamReader isr = new InputStreamReader(groovy);
        Script script = groovyShell.parse(isr);
        //add direct use of binder methods
        DefaultGroovyMethods.mixin(script.getMetaClass(), BinderDelegate.class);
        script.invokeMethod("setBinder", binder());
        //go!
        script.run();
    }


    public static class BinderDelegate extends OverrideBinder {

        Binder binder;

        public Binder binder() {
            return binder;
        }

        public void bindProperties(Map<String, String> props) {
            PropertyModule.bindProperties(binder, props);
        }

        public void setBinder(Binder binder) {
            this.binder = binder;
        }

        @Override
        public void bindInterceptor(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher, MethodInterceptor... interceptors) {
            binder.bindInterceptor(classMatcher, methodMatcher, interceptors);
        }

        @Override
        public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
            binder.bindScope(annotationType, scope);
        }

        @Override
        public <T> LinkedBindingBuilder<T> bind(Key<T> key) {
            return binder.bind(key);
        }

        @Override
        public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
            return binder.bind(typeLiteral);
        }

        @Override
        public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
            return binder.bind(type);
        }

        @Override
        public AnnotatedConstantBindingBuilder bindConstant() {
            return binder.bindConstant();
        }

        @Override
        public <T> void requestInjection(TypeLiteral<T> type, T instance) {
            binder.requestInjection(type, instance);
        }

        @Override
        public void requestInjection(Object instance) {
            binder.requestInjection(instance);
        }

        @Override
        public void requestStaticInjection(Class<?>... types) {
            binder.requestStaticInjection(types);
        }

        @Override
        public void install(Module module) {
            binder.install(module);
        }

        @Override
        public Stage currentStage() {
            return binder.currentStage();
        }

        @Override
        public void addError(String message, Object... arguments) {
            binder.addError(message, arguments);
        }

        @Override
        public void addError(Throwable t) {
            binder.addError(t);
        }

        @Override
        public void addError(Message message) {
            binder.addError(message);
        }

        @Override
        public <T> Provider<T> getProvider(Key<T> key) {
            return binder.getProvider(key);
        }

        @Override
        public <T> Provider<T> getProvider(Class<T> type) {
            return binder.getProvider(type);
        }

        @Override
        public <T> MembersInjector<T> getMembersInjector(TypeLiteral<T> typeLiteral) {
            return binder.getMembersInjector(typeLiteral);
        }

        @Override
        public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
            return binder.getMembersInjector(type);
        }

        @Override
        public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher, TypeConverter converter) {
            binder.convertToTypes(typeMatcher, converter);
        }

        @Override
        public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
            binder.bindListener(typeMatcher, listener);
        }

        @Override
        public Binder withSource(Object source) {
            return binder.withSource(source);
        }

        @Override
        public Binder skipSources(Class... classesToSkip) {
            return binder.skipSources(classesToSkip);
        }

        @Override
        public PrivateBinder newPrivateBinder() {
            return binder.newPrivateBinder();
        }

        @Override
        public void requireExplicitBindings() {
            binder.requireExplicitBindings();
        }

        @Override
        public void disableCircularProxies() {
            binder.disableCircularProxies();
        }
    }

}
