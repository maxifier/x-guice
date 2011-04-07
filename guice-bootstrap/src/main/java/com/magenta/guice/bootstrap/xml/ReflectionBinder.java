package com.magenta.guice.bootstrap.xml;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;
import com.magenta.guice.bootstrap.ClassUtils;
import com.magenta.guice.bootstrap.model.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ReflectionBinder {

    public static final Logger logger = LoggerFactory.getLogger(ReflectionBinder.class);

    private final ClassLoader classLoader;
    private final Binder binder;


    public ReflectionBinder(Binder binder, ClassLoader classLoader) {
        this.binder = binder;
        this.classLoader = classLoader;
    }


    @SuppressWarnings({"unchecked"})
    public void bindConstant(String annotation, String named, String value) {
        if (annotation != null) {
            try {
                Class annotationClass = loadClass(annotation);
                binder.bindConstant().annotatedWith(annotationClass).to(value);
                logger.info("Constant '{}' has been bound with annotation '{}'", value, annotation);
            } catch (ClassNotFoundException e) {
                binder.addError(String.format("Unable to find annotation class %s", annotation), e);
            }
        } else if (named != null) {
            binder.bindConstant().annotatedWith(Names.named(named)).to(value);
            logger.info("Constant '{}' has been bound with annotation '@Named(\"{}\")'", value, named);
        } else {
            binder.addError("Unable to bind constant without <annotation> or <named> tags." +
                    " Use <annotation> for self-made annotations or <named> for @Named(\"<name>\") annotations");
        }
    }

    @SuppressWarnings({"unchecked"})
    public void bindModule(String moduleClassName) {
        try {
            Class moduleClass = loadClass(moduleClassName);
            binder.install((Module) moduleClass.newInstance());
            logger.info("Module '{}' has been added to configuration", moduleClassName);
        } catch (ClassNotFoundException e) {
            binder.addError(String.format("Unable to find module class %s", moduleClassName), e);
        } catch (InstantiationException e) {
            binder.addError(String.format("Unable to instantiate module class %s", moduleClassName), e);
        } catch (IllegalAccessException e) {
            binder.addError(String.format("Illegal access to module class %s", moduleClassName), e);
        } catch (ClassCastException e) {
            binder.addError(
                    String.format("Module class %s is not an instance of 'com.google.inject.Module'", moduleClassName),
                    e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public void bindComponent(String interfaceName, String annotationName, String className,
                              String scope, boolean eager) {
        ScopedBindingBuilder builder;
        Class classA;
        try {
            classA = loadClass(className);
        } catch (ClassNotFoundException e) {
            binder.addError(String.format("Unable to find component class %s", className), e);
            return;
        }
        if (interfaceName == null) {
            builder = binder.bind(classA);
            logger.info("Component '{}' has been bound to itself", className);
        } else {
            Class interfaceA = null;
            try {
                interfaceA = loadClass(interfaceName);
            } catch (ClassNotFoundException e) {
                binder.addError(String.format("Unable to find component interface %s", interfaceName), e);
                return;
            }
            if (annotationName == null) {
                builder = binder.bind(interfaceA).to(classA);
                logger.info("Component '{}' has been bound to '{}'", new Object[]{className, interfaceName});
            } else {
                Class annotation;
                try {
                    annotation = loadClass(annotationName);
                } catch (ClassNotFoundException e) {
                    binder.addError(String.format("Unable to find annotation class %s", annotationName), e);
                    return;
                }
                builder = binder.bind(interfaceA).annotatedWith(annotation).to(classA);
                logger.info("Component '{}' has been bound to '{}' annotated with '{}' ", new Object[]{className, interfaceName, annotationName});
            }
        }
        if (eager) {
            builder.asEagerSingleton();
        } else if (scope != null && scope.equals(Component.SINGLETON)) {
            builder.in(Scopes.SINGLETON);
        } else if (scope != null && scope.equals(Component.NO_SCOPE)) {
            builder.in(Scopes.NO_SCOPE);
        }
    }

    private Class loadClass(String name) throws ClassNotFoundException {
        if (classLoader == null) {
            return ClassUtils.forName(name);
        } else {
            return classLoader.loadClass(name);
        }
    }


}