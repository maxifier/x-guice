/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * @author Konstantin Lyamshin (2015-12-30 1:11)
 */
public class PropertyModuleTest extends org.testng.Assert {

    @Test
    public void testLoadFromModule() throws Exception {
        PropertyModule module = PropertyModule.loadFrom(new TestSampleModule());

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "foo", "bar", "baz", "foo-array", "bar-array", "baz-array");
    }

    static void assertPropertiesList(List<PropertyDefinition> actual, String... expected) {
        HashSet<String> names = new HashSet<String>(Arrays.asList(expected));
        for (PropertyDefinition definition : actual) {
            assertTrue(names.remove(definition.getName()), definition + " not expected to be in list");
        }
        assertTrue(names.isEmpty(), names + " expected to be in list but don't");
    }

    @Test
    public void testLoadFromModuleClass() throws Exception {
        PropertyModule module = PropertyModule.loadFrom(TestSampleModule.class);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "foo", "bar", "baz", "foo-array", "bar-array", "baz-array");
    }

    @Test
    public void testLoadFromModuleClassRoot() throws Exception {
        PropertyModule module = PropertyModule.loadFrom(TestSampleModule2.class);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "fooz", "barz", "bazz");
    }

    @Test
    public void testLoadFromCustomResource() throws Exception {
        PropertyModule module = PropertyModule.loadFrom("custom.properties");

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "foo", "bar", "baz");
    }

    @Test
    public void testLoadFromStream() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/custom.properties");
        assertNotNull(stream);

        PropertyModule module = PropertyModule.loadFrom(stream);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "foo", "bar", "baz");
    }

    @Test
    public void testLoadFromReader() throws Exception {
        InputStream stream = getClass().getResourceAsStream("/custom.properties");
        assertNotNull(stream);

        Reader reader = new InputStreamReader(stream, "UTF-8");

        PropertyModule module = PropertyModule.loadFrom(reader);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "foo", "bar", "baz");
    }

    @Test
    public void testLoadFromProperties() throws Exception {
        Properties props = new Properties();
        props.put("aaa", "a");
        props.put("bbb", "b");
        props.put("ccc", "c");

        PropertyModule module = PropertyModule.loadFrom(props);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "aaa", "bbb", "ccc");
    }

    @Test
    public void testLoadFromMap() throws Exception {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("ddd", "d");
        props.put("eee", "e");
        props.put("fff", "f");

        PropertyModule module = PropertyModule.loadFrom(props);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "ddd", "eee", "fff");
    }

    @Test
    public void testLoadFromDefinitions() throws Exception {
        ImmutableSet<PropertyDefinition> definitions = ImmutableSet.of(
            new PropertyDefinition("p1", "v1", ""),
            new PropertyDefinition("p2", "v2", ""),
            new PropertyDefinition("p3", "v3", "")
        );

        PropertyModule module = PropertyModule.loadFrom(definitions);

        ImmutableList<PropertyDefinition> properties = module.getProperties();
        assertPropertiesList(properties, "p1", "p2", "p3");
    }

    @Test
    public void testConfigure() throws Exception {
        Injector injector = Guice.createInjector(PropertyModule.loadFrom("custom.properties"));
        TestSampleConfigSimple config = injector.getInstance(TestSampleConfigSimple.class);
        assertEquals(config.foo, "custom val");
        assertEquals(config.sbar, "7");
        assertEquals(config.bar, 7);
        assertEquals(config.baz, false);
    }

    @Test
    public void testConfigureWithConverters() throws Exception {
        Injector injector = Guice.createInjector(new TestSampleModule());
        TestSampleConfig config = injector.getInstance(TestSampleConfig.class);
        assertEquals(config.foo, "foo val");
        assertEquals(config.sbar, "2");
        assertEquals(config.bar, 2);
        assertEquals(config.baz, true);
        assertEquals(config.afoo, new String[] {"foo1", "foo2", "foo3"});
        assertEquals(config.abar, new int[] {1, 2, 3});
        assertEquals(config.abaz, new boolean[] {true, true, false});
    }

    static class TestSampleModule extends AbstractModule {
        @Override
        protected void configure() {
            install(PropertyModule.loadFrom(this).withConverters());
            bind(TestSampleConfig.class);
        }
    }

    static abstract class TestSampleModule2 implements Module {
    }

    static class TestSampleConfigSimple {
        @Inject
        @Property("foo")
        String foo;

        @Inject
        @Property("bar")
        int bar;

        @Inject
        @Property("bar")
        String sbar;

        @Inject
        @Property("baz")
        boolean baz;
    }

    static class TestSampleConfig {
        @Inject
        @Property("foo")
        String foo;

        @Inject
        @Property("bar")
        int bar;

        @Inject
        @Property("bar")
        String sbar;

        @Inject
        @Property("baz")
        boolean baz;

        @Inject
        @Property("foo-array")
        String[] afoo;

        @Inject
        @Property("bar-array")
        int[] abar;

        @Inject
        @Property("baz-array")
        boolean[] abaz;
    }
}


