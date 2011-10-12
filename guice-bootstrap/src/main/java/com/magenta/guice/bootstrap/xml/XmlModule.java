package com.magenta.guice.bootstrap.xml;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;
import com.magenta.guice.bootstrap.model.Component;
import com.magenta.guice.bootstrap.model.Constant;
import com.magenta.guice.bootstrap.model.Guice;
import com.magenta.guice.bootstrap.model.Property;
import com.magenta.guice.bootstrap.model.io.xpp3.XGuiceBootstrapXpp3Reader;
import com.magenta.guice.property.PropertyModule;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 17:30:26
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
@SuppressWarnings({"unchecked"})
public class XmlModule extends AbstractModule {

    private final InputStream inputStream;
    private final ClassLoader classLoader;

    public XmlModule(InputStream inputStream) {
        this.inputStream = inputStream;
        this.classLoader = this.getClass().getClassLoader();
    }

    public XmlModule(InputStream inputStream, ClassLoader classLoader) {
        this.inputStream = inputStream;
        this.classLoader = classLoader;
    }


    @Override
    protected void configure() {
        Guice guice;
        try {
            guice = parseConfiguration();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ReflectionBinder reflectionBinder = new ReflectionBinder(binder(), classLoader);
        bindProperties(guice);
        bindModules(guice, reflectionBinder);
        bindComponent(guice, reflectionBinder);
        bindConstant(guice, reflectionBinder);
    }

    private void bindProperties(Guice guice) {
        List<Property> properties = guice.getProperties();
        Map<String, String> propertiesMap = new HashMap<String, String>(properties.size());
        for (Property property : properties) {
            String old = propertiesMap.put(property.getName(), property.getValue());
            if (old != null) {
                throw new IllegalStateException("Multiple properties with name " + property.getName() + " exists");
            }
        }
        PropertyModule.bindProperties(binder(), propertiesMap);
    }

    private void bindComponent(Guice guice, ReflectionBinder reflectionBinder) {
        List<Component> components = guice.getComponents();
        for (Component component : components) {
            reflectionBinder.bindComponent(component.getIface(), component.getAnnotation(), component.getClazz(),
                    component.getScope(), component.isEager());
        }
    }

    private void bindConstant(Guice guice, ReflectionBinder reflectionBinder) {
        List<Constant> constants = guice.getConstants();
        for (Constant constant : constants) {
            reflectionBinder.bindConstant(constant.getAnnotation(), constant.getNamed(), constant.getValue());
        }
    }

    private void bindModules(Guice guice, ReflectionBinder reflectionBinder) {
        List<String> modules = guice.getModules();
        for (String module : modules) {
            reflectionBinder.bindModule(module);
        }
    }

    private Guice parseConfiguration() throws IOException {
        XGuiceBootstrapXpp3Reader reader = new XGuiceBootstrapXpp3Reader();
        Guice guice;
        try {
            guice = reader.read(inputStream);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Unable to parse input stream", e);
        }
        return guice;
    }

    static final class ReflectionBinder {

        public static final Logger logger = LoggerFactory.getLogger(ReflectionBinder.class);

        private final ClassLoader classLoader;
        private final Binder binder;


        public ReflectionBinder(Binder binder,
                                ClassLoader classLoader) {
            this.binder = binder;
            this.classLoader = classLoader;
        }


        @SuppressWarnings({"unchecked"})
        public void bindConstant(String annotation, String named, String value) {
            if (annotation != null) {
                try {
                    Class annotationClass = classLoader.loadClass(annotation);
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
                Class moduleClass = classLoader.loadClass(moduleClassName);
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
                classA = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                binder.addError(String.format("Unable to find component class %s", className), e);
                return;
            }
            if (interfaceName == null) {
                builder = binder.bind(classA);
                logger.info("Component '{}' has been bound to itself", className);
            } else {
                Class interfaceA;
                try {
                    interfaceA = classLoader.loadClass(interfaceName);
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
                        annotation = classLoader.loadClass(annotationName);
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

    }
}

