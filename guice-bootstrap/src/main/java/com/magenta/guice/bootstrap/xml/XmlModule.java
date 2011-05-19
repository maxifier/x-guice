package com.magenta.guice.bootstrap.xml;

import com.google.inject.AbstractModule;
import com.magenta.guice.bootstrap.ResourceUtils;
import com.magenta.guice.bootstrap.model.Component;
import com.magenta.guice.bootstrap.model.Constant;
import com.magenta.guice.bootstrap.model.Guice;
import com.magenta.guice.bootstrap.model.Property;
import com.magenta.guice.bootstrap.model.io.xpp3.XGuiceBootstrapXpp3Reader;
import com.magenta.guice.property.PropertyModule;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

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

    private final String xmlCfgPath;
    private final ClassLoader classLoader;

    public XmlModule(String xmlCfgPath) {
        this.xmlCfgPath = xmlCfgPath;
        this.classLoader = null;
    }

    public XmlModule(String xmlCfgPath, ClassLoader classLoader) {
        this.xmlCfgPath = xmlCfgPath;
        this.classLoader = classLoader;
    }

    @Override
    protected void configure() {
        InputStream cfgStream = readConfiguration();
        Guice guice = parseConfiguration(cfgStream);
        ResourceUtils.close(cfgStream);
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

    private Guice parseConfiguration(InputStream is) {
        XGuiceBootstrapXpp3Reader reader = new XGuiceBootstrapXpp3Reader();
        Guice guice;
        try {
            guice = reader.read(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read resource " + xmlCfgPath, e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Unable to parse resource " + xmlCfgPath, e);
        }
        return guice;
    }

    private InputStream readConfiguration() {
        InputStream is;
        try {
            is = ResourceUtils.getInputStreamForPath(xmlCfgPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to find resource " + xmlCfgPath, e);
        }
        return is;
    }

    @Override
    public String toString() {
        return "XmlModule{" +
                "xmlCfg='" + xmlCfgPath + '\'' +
                '}';
    }
}

