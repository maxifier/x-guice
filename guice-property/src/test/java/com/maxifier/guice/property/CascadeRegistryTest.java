/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.spi.Elements;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * @author Konstantin Lyamshin (2015-12-30 1:11)
 */
public class CascadeRegistryTest extends org.testng.Assert {

    @Test
    public void testReadDefaultsFrom() throws Exception {
        final PropertyDefinition prop1 = new PropertyDefinition("prop1", "val1", "first property");
        final PropertyDefinition prop2 = new PropertyDefinition("prop2", "val2", "second property");
        final PropertyDefinition prop3 = new PropertyDefinition("prop3", "val3", "third property");
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                install(PropertyModule.loadFrom(ImmutableSet.of(
                    prop1,
                    prop2,
                    prop3
                )));
            }
        };

        CascadeRegistry registry1 = new CascadeRegistry.Builder()
            .withDefaults(module)
            .build();

        assertEquals(registry1.keys(), ImmutableSet.of("prop1", "prop2", "prop3"));
        assertPropertyEquals(registry1.getDefinition("prop1"), prop1);
        assertPropertyEquals(registry1.getDefinition("prop2"), prop2);
        assertPropertyEquals(registry1.getDefinition("prop3"), prop3);
        assertEquals(registry1.get("prop1"), prop1.getValue());
        assertEquals(registry1.get("prop2"), prop2.getValue());
        assertEquals(registry1.get("prop3"), prop3.getValue());

        CascadeRegistry registry2 = new CascadeRegistry.Builder()
            .withDefaults(Elements.getElements(module))
            .build();

        assertEquals(registry2.keys(), ImmutableSet.of("prop1", "prop2", "prop3"));
        assertPropertyEquals(registry2.getDefinition("prop1"), prop1);
        assertPropertyEquals(registry2.getDefinition("prop2"), prop2);
        assertPropertyEquals(registry2.getDefinition("prop3"), prop3);
        assertEquals(registry2.get("prop1"), prop1.getValue());
        assertEquals(registry2.get("prop2"), prop2.getValue());
        assertEquals(registry2.get("prop3"), prop3.getValue());
    }

    static void assertPropertyEquals(PropertyDefinition p1, PropertyDefinition p2) {
        assertEquals(p1.getName(), p2.getName());
        assertEquals(p1.getValue(), p2.getValue());
        assertEquals(p1.getComment(), p2.getComment());
    }

    @Test
    public void testReadDefaultsFromProperties() throws Exception {
        Properties props = new Properties();
        props.put("p1", "v1");
        props.put("p2", "v2");

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(props)
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2"));
        assertPropertyEquals(registry.getDefinition("p1"), new PropertyDefinition("p1", "v1", ""));
        assertPropertyEquals(registry.getDefinition("p2"), new PropertyDefinition("p2", "v2", ""));
        assertEquals(registry.get("p1"), "v1");
        assertEquals(registry.get("p2"), "v2");
    }

    @Test
    public void testReadDefaultsFromMap() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("pr1", "vl1", "pr2", "vl2"))
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("pr1", "pr2"));
        assertPropertyEquals(registry.getDefinition("pr1"), new PropertyDefinition("pr1", "vl1", ""));
        assertPropertyEquals(registry.getDefinition("pr2"), new PropertyDefinition("pr2", "vl2", ""));
        assertEquals(registry.get("pr1"), "vl1");
        assertEquals(registry.get("pr2"), "vl2");
    }

    @Test
    public void testReadDefaulsFromDescriptors() throws Exception {
        PropertyDefinition prop1 = new PropertyDefinition("prop1", "val1", "first property");
        PropertyDefinition prop2 = new PropertyDefinition("prop2", "val2", "second property");

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableSet.of(prop1, prop2))
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("prop1", "prop2"));
        assertPropertyEquals(registry.getDefinition("prop1"), prop1);
        assertPropertyEquals(registry.getDefinition("prop2"), prop2);
        assertEquals(registry.get("prop1"), "val1");
        assertEquals(registry.get("prop2"), "val2");
    }

    @Test
    public void testLoadOverridesFromProperties() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("p1", "v1");
        properties.setProperty("p2", "v2");
        properties.setProperty("p4", "v4");

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "def1", "p2", "def2", "p3", "def3"))
            .withOverrides(properties)
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "p3"));
        assertEquals(registry.get("p1"), "v1");
        assertEquals(registry.get("p2"), "v2");
        assertEquals(registry.get("p3"), "def3");
        assertEquals(registry.get("p4"), null); // ignore values without default
    }

    @Test
    public void testLoadOverridesFromMap() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "def1", "p2", "def2", "p3", "def3"))
            .withOverrides(ImmutableMap.of("p2", "val2", "p4", "val4"))
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "p3"));
        assertEquals(registry.get("p1"), "def1");
        assertEquals(registry.get("p2"), "val2");
        assertEquals(registry.get("p3"), "def3");
        assertEquals(registry.get("p4"), null); // ignore values without default
    }

    @Test
    public void testCustomOverrider() throws Exception {
        CascadeRegistry.Overrider overrider = mock(CascadeRegistry.Overrider.class);
        when(overrider.override("p1", "def1")).thenReturn("over1");

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "def1", "p2", "def2", "p3", "def3"))
            .withOverrider(overrider)
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "p3"));
        assertEquals(registry.get("p1"), "over1");
        assertEquals(registry.get("p1"), "over1"); // check that overriding processed only once
        assertEquals(registry.get("p2"), "def2");
        assertEquals(registry.get("p3"), "def3");
        assertEquals(registry.get("p4"), null);

        verify(overrider).override("p1", "def1");
        verify(overrider).override("p2", "def2");
        verify(overrider).override("p3", "def3");
        verifyNoMoreInteractions(overrider);
    }

    @Test
    public void testCustomOverriderOverOverrideProperties() throws Exception {
        CascadeRegistry.Overrider overrider = mock(CascadeRegistry.Overrider.class);
        when(overrider.override("p2", "val2")).thenReturn("over2");

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "def1", "p2", "def2", "p3", "def3"))
            .withOverrides(ImmutableMap.of("p1", "val1", "p2", "val2"))
            .withOverrider(overrider)
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "p3"));
        assertEquals(registry.get("p1"), "val1");
        assertEquals(registry.get("p1"), "val1"); // check that overriding processed only once
        assertEquals(registry.get("p2"), "over2");
        assertEquals(registry.get("p3"), "def3");
        assertEquals(registry.get("p4"), null);

        verify(overrider).override("p1", "val1");
        verify(overrider).override("p2", "val2");
        verify(overrider).override("p3", "def3");
        verifyNoMoreInteractions(overrider);
    }

    @Test
    public void testSystemPropertiesOverrider() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("java.version", "", "p", "def"))
            .withSystemProperties()
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("java.version", "p"));
        assertEquals(registry.get("java.version"), System.getProperty("java.version"));
        assertEquals(registry.get("java.home"), null); // skip properties without default
        assertEquals(registry.get("p"), "def");
    }

    @Test
    public void testEnvironmentOverrider() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("PATH", "", "p", "def"))
            .withEnvironmentProperties()
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("PATH", "p"));
        assertEquals(registry.get("PATH"), System.getenv("PATH"));
        assertEquals(registry.get("HOME"), null); // skip properties without default
        assertEquals(registry.get("p"), "def");
    }

    @Test
    public void testSystemPropertiesAndEnvironmentOverrider() throws Exception {
        String oldval = System.getProperty("PATH");
        try {
            System.setProperty("PATH", "xxx");
            CascadeRegistry registry = new CascadeRegistry.Builder()
                .withDefaults(ImmutableMap.of("PATH", "", "HOME", "", "p", "def"))
                .withEnvironmentProperties()
                .withSystemProperties()
                .build();

            assertEquals(registry.keys(), ImmutableSet.of("PATH", "HOME", "p"));
            assertEquals(registry.get("HOME"), System.getenv("HOME")); // environment overriding
            assertEquals(registry.get("PATH"), "xxx"); // system properties overrides environment
            assertEquals(registry.get("java.home"), null); // skip properties without default
            assertEquals(registry.get("p"), "def");
        } finally {
            if (oldval != null) {
                System.setProperty("PATH", oldval);
            } else {
                System.clearProperty("PATH");
            }
        }
    }

    @Test
    public void testInterpolation() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v1", "p2", "v2", "p3", "v3"))
            .withOverrides(ImmutableMap.of("p1", "val1${p2}", "p2", "val2${p3}"))
            .withInterpolation()
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "p3"));
        assertEquals(registry.get("p1"), "val1val2v3");
        assertEquals(registry.get("p2"), "val2v3");
        assertEquals(registry.get("p3"), "v3");
    }

    @Test
    public void testInterpolationMarker() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableSet.of(
                new PropertyDefinition("p1", "v1", "@interpolate"),
                new PropertyDefinition("p2", "v2", "@interpolate"),
                new PropertyDefinition("p3", "v3", ""),
                new PropertyDefinition("p4", "v4", "")
            ))
            .withOverrides(ImmutableMap.of(
                "p1", "val1='${p2}','${p3}'",
                "p2", "val2='${p3}'",
                "p3", "val3='${p4}'"
            ))
            .withInterpolation("@interpolate")
            .build();

        assertEquals(registry.get("p1"), "val1='val2='val3='${p4}''','val3='${p4}''");
        assertEquals(registry.get("p2"), "val2='val3='${p4}''");
        assertEquals(registry.get("p3"), "val3='${p4}'");
        assertEquals(registry.get("p4"), "v4");
    }

    @Test
    public void testCustomInterpolator() throws Exception {
        CascadeRegistry.Interpolator interpolator = mock(CascadeRegistry.Interpolator.class);
        when(interpolator.interpolate(anyString(), anyString(), ArgumentMatchers.<String, String>anyMap())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock inv) throws Throwable {
                return inv.getArgument(1);
            }
        });
        when(interpolator.interpolate(eq("p2"), eq("v2"), ArgumentMatchers.<String, String>anyMap())).thenReturn("inter2");

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v1", "p2", "v2", "p3", "v3"))
            .withInterpolation(interpolator)
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "p3"));
        assertEquals(registry.get("p1"), "v1");
        assertEquals(registry.get("p2"), "inter2");
        assertEquals(registry.get("p3"), "v3");

        verify(interpolator).interpolate(eq("p1"), eq("v1"), ArgumentMatchers.<String, String>anyMap());
        verify(interpolator).interpolate(eq("p2"), eq("v2"), ArgumentMatchers.<String, String>anyMap());
        verify(interpolator).interpolate(eq("p3"), eq("v3"), ArgumentMatchers.<String, String>anyMap());
        verifyNoMoreInteractions(interpolator);
    }

    @Test
    public void testInterpolationOverOverriderAndOverridingProperties() throws Exception {

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v${p2}", "p2", "v2", "java.version", ""))
            .withOverrides(ImmutableMap.of("p2", "=${java.version}", "p3", "v3"))
            .withSystemProperties()
            .withInterpolation()
            .build();

        assertEquals(registry.keys(), ImmutableSet.of("p1", "p2", "java.version"));
        assertEquals(registry.get("p1"), "v=" + System.getProperty("java.version"));
        assertEquals(registry.get("p2"), "=" + System.getProperty("java.version"));
        assertEquals(registry.get("java.version"), System.getProperty("java.version"));
        assertEquals(registry.get("java.home"), null); // skip properties without defaults
        assertEquals(registry.get("p3"), null); // skip properties without defaults
    }

    @Test
    public void testCustomPersister() throws Exception {
        CascadeRegistry.Persister persister = mock(CascadeRegistry.Persister.class);

        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v1", "p2", "v2", "p3", "v3"))
            .withPersister(persister)
            .build();

        registry.store();

        registry.set("p2", "v22");
        registry.set("p3", "v33");
        registry.store();

        registry.store();

        verify(persister).persist(ImmutableMap.of("p2", "v22", "p3", "v33"));
        verifyNoMoreInteractions(persister);
    }

    @Test
    public void testGetOrDefault() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v1", "p2", "v2", "p3", "v3"))
            .build();

        assertEquals(registry.get("p1"), "v1");
        assertEquals(registry.get("p4"), null);
        assertEquals(registry.getOrDefault("p1", "v"), "v1");
        assertEquals(registry.getOrDefault("p4", "v"), "v");
    }

    @Test
    public void testInterpolationAfterSet() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v=${p2}", "p2", "${p3}", "p3", "v3"))
            .withInterpolation()
            .build();

        assertEquals(registry.get("p1"), "v=v3");
        assertEquals(registry.get("p2"), "v3");

        registry.set("p3", "new");
        assertEquals(registry.get("p1"), "v=new");
        assertEquals(registry.get("p2"), "new");

        registry.set("p2", "xxx");
        assertEquals(registry.get("p1"), "v=xxx");
        assertEquals(registry.get("p2"), "xxx");

        registry.set("p1", "v=${p3}");
        assertEquals(registry.get("p1"), "v=new");
        assertEquals(registry.get("p2"), "xxx");
    }

    @Test
    public void testProcessOverridesWithModule() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v1", "p2", "1", "p3", "1.0"))
            .withOverrides(ImmutableMap.of("p1", "xxx", "p2", "2", "p3", "3.0"))
            .build();

        Injector injector = Guice.createInjector(registry.applyOverrides(new TestModule()));

        TestClass instance = injector.getInstance(TestClass.class);
        assertEquals(instance.p1, "xxx");
        assertEquals(instance.p2, 2);
        assertEquals(instance.p3, 3.0);
    }

    static class TestClass {
        @Inject
        @Property("p1")
        String p1;

        @Inject
        @Property("p2")
        int p2;

        @Inject
        @Property("p3")
        double p3;
    }

    static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(PropertyModule.loadFrom(ImmutableSet.of(
                new PropertyDefinition("p1", "def1", ""),
                new PropertyDefinition("p2", "-1", ""),
                new PropertyDefinition("p3", "-1", "")
            )));

            bind(TestClass.class);
        }
    }

    @Test
    public void testAsModule() throws Exception {
        CascadeRegistry registry = new CascadeRegistry.Builder()
            .withDefaults(ImmutableMap.of("p1", "v1", "p2", "v2", "p3", "v3"))
            .build();

        Registry instance = Guice.createInjector(registry.asModule()).getInstance(Registry.class);
        assertSame(instance, registry);
    }

    @Test
    public void testDefaultInterpolator() throws Exception {
        ImmutableMap<String, String> registry = ImmutableMap.of(
            "client", "google",
            "client_name", "${client}",
            "product", "${client_trademark} DFP",
            "client_trademark", "${client_name}'s ${product}",
            "cyclic", "${cyclic} ${product}"
        );

        CascadeRegistry.DefaultInterpolator interpolator = new CascadeRegistry.DefaultInterpolator();
        assertEquals(interpolator.interpolate("mail", "${client}@gmail.com", registry), "google@gmail.com");
        assertEquals(interpolator.interpolate("mail", "${client_name}@gmail.com", registry), "google@gmail.com");
        assertEquals(interpolator.interpolate("smth", "${client_name}@${client}.com", registry), "google@google.com");
        assertEquals(interpolator.interpolate("mail", "${client_name}@${gmail}.com", registry), "google@${gmail}.com");
        assertEquals(interpolator.interpolate("mail", "}${${client_name}@gmail}.com", registry), "}${google@gmail}.com");
        assertEquals(interpolator.interpolate("cycl", "${cyclic}'s ${client}", registry), "${cyclic}'s ${client}"); // cyclic
    }

    @Test
    public void testDefaultInterpolator2() throws Exception {
        ImmutableMap<String, String> registry = ImmutableMap.of(
            "prop.prop1", "v$1",
            "prop_prop2", "v$2",
            "prop-prop3", "v$3",
            "prop4", "v4"
        );
        CascadeRegistry.DefaultInterpolator interpolator = new CascadeRegistry.DefaultInterpolator();
        assertEquals(interpolator.interpolate("x", "start ${prop.prop1}.${prop_prop2}(${prop-prop3});", registry), "start v$1.v$2(v$3);");
        assertEquals(interpolator.interpolate("x", "start ${prop4};", registry), "start v4;");
        assertEquals(interpolator.interpolate("x", "start ${prop.prop1}.$propX(${propY});", registry), "start v$1.$propX(${propY});");
    }
}
