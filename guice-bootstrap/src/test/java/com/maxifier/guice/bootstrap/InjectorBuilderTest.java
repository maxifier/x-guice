/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.common.collect.Ordering;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.google.inject.util.Types;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author Konstantin Lyamshin (2015-11-05 20:06)
 */
public class InjectorBuilderTest extends org.testng.Assert {
    InjectorBuilder builder;

    @BeforeMethod
    public void setUp() throws Exception {
        Properties configuration = new Properties();
        configuration.load(this.getClass().getResourceAsStream("/config.properties"));
        builder = new InjectorBuilder().withConfiguration(configuration);
    }

    @Test
    public void testBuildModule() throws Exception {
        Module module = builder
            .withModule(new SpecificModule1())
            .buildApplicationModule();
        boolean found = false;
        for (Element element : Elements.getElements(module)) {
            found |= element.acceptVisitor(new DefaultElementVisitor<Boolean>() {
                @Override
                public <T> Boolean visit(Binding<T> binding) {
                    return binding.getKey().equals(Key.get(Foo.class));
                }
            });
        }
        assertEquals(found, true);
    }

    @Test
    public void testBuildInjector() throws Exception {
        Injector injector = builder
            .withModule(new SpecificModule1())
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImpl{}");
    }

    @Test(dependsOnMethods = "testBuildInjector")
    public void testNoConfig() throws Exception {
        Injector injector = new InjectorBuilder()
            .withModule(new SpecificModule1())
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImpl{}");
    }

    @Test(dependsOnMethods = "testBuildInjector")
    public void testCustomBundle() throws Exception {
        builder.withModuleBundle(bundle("custom", SpecificModule1.class, ConfiguredModule.class));
        Injector injector = builder.buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImpl{}");
        assertEquals(injector.getInstance(List.class).toString(), "[]");
    }

    @Test(dependsOnMethods = "testCustomBundle", expectedExceptions = IllegalArgumentException.class)
    public void testWrongModule() throws Exception {
        builder.withModuleBundle(bundle("custom", WrongModule.class));
        builder.buildConfigurationInjector();
    }

    @Test(dependsOnMethods = "testCustomBundle")
    public void testConfiguredModule() throws Exception {
        builder.withModuleBundle(bundle("custm", SpecificModule1.class));
        builder.withModuleBundle(bundle("custom", ConfiguredModule.class));
        Injector injector = builder.buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Key.get(String.class, Names.named("config"))), "b=[custm],m=[SpecificModule1{}],p=cscfg,c=ijcfg");
    }

    @Test(dependsOnMethods = "testCustomBundle")
    public void testConfiguredProvider() throws Exception {
        builder.withModuleBundle(bundle("custp", SpecificModule1.class));
        builder.withModuleBundle(bundle("custom", ConfiguredProvider.class));
        Injector injector = builder.buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Key.get(String.class, Names.named("config"))), "b=[custp],m=[SpecificModule1{}],p=cscfg,c=ijcfg");
    }

    @Test(dependsOnMethods = "testConfiguredProvider", expectedExceptions = IllegalArgumentException.class)
    public void testWrongProvider() throws Exception {
        builder.withModuleBundle(bundle("custom", WrongProvider.class));
        builder.buildConfigurationInjector();
    }

    @Test(dependsOnMethods = "testCustomBundle")
    public void testStringBundle() throws Exception {
        Injector injector = builder
            .withModuleBundle("com.maxifier.guice.bootstrap.Bundle.cust")
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImpl{}");
        assertEquals(injector.getInstance(List.class).toString(), "[]");
    }

    @Test(dependsOnMethods = "testCustomBundle")
    public void testEnumBundle() throws Exception {
        Injector injector = builder
            .withModuleBundle(Bundle.cust)
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImpl{}");
        assertEquals(injector.getInstance(List.class).toString(), "[]");
    }

    @Test(dependsOnMethods = "testEnumBundle")
    public void testImplements() throws Exception {
        Injector injector = builder.withModuleBundle(Bundle.impl).buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "FooImpl{}");
    }

    @Test(dependsOnMethods = "testEnumBundle")
    public void testExtends() throws Exception {
        Injector injector = builder.withModuleBundle(Bundle.ext).buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "FooImpl2{}");
    }

    @Test(dependsOnMethods = "testEnumBundle")
    public void testOverrides() throws Exception {
        Injector injector = builder.withModuleBundle(Bundle.over).buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "FooImplEx{}");
    }

    @Test(dependsOnMethods = "testImplements")
    public void testModuleDup() throws Exception {
        Module module = new ConfiguredModule();
        Injector injector = builder
            .withModule(module)
            .withModule(module)
            .withModuleBundle(Bundle.impl)
            .withModuleBundle(Bundle.impl)
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImpl{}");
        assertEquals(injector.getInstance(List.class).toString(), "[]");
    }

    @Test(dependsOnMethods = "testNoConfig")
    public void testPluginNoConfig() throws Exception {
        Injector injector = new InjectorBuilder()
            .withModule(new SpecificModule1())
            .withPlugin(new PluginModule())
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).name(), "FooImplEx{}");
    }

    private static File lookupPluginDir() {
        URL url = InjectorBuilderTest.class.getResource("/");
        assertEquals(url.getProtocol(), "file");
        assertTrue(url.getPath().endsWith("/target/test-classes/"));
        return new File(url.getPath(), "../../src/test/jars");
    }

    @Test(dependsOnMethods = "testPluginNoConfig")
    public void testCustomPlugin() throws Exception {
        Injector injector = builder
            .withModuleBundle(Bundle.impl)
            .withPluginDir(lookupPluginDir())
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.Plugin1"))
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "Foo1{}");
    }

    @Test(dependsOnMethods = "testNoConfig")
    public void testStringPlugin() throws Exception {
        Injector injector = builder
            .withModuleBundle(Bundle.impl)
            .withPluginDir(lookupPluginDir())
            .withPluginBundle("com.maxifier.guice.bootstrap.Bundle.plug")
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "Foo1{}");
    }

    @Test(dependsOnMethods = "testNoConfig")
    public void testEnumPlugin() throws Exception {
        Injector injector = builder
            .withPluginDir(lookupPluginDir())
            .withModuleBundle(Bundle.impl)
            .withPluginBundle(Bundle.plug)
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "Foo1{}");
    }

    @Test(dependsOnMethods = "testStringPlugin", expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*not found.*")
    public void testWrongPlugin1() throws Exception {
        builder
            .withPluginDir(lookupPluginDir())
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.MissedPlugin"));
    }

    @Test(dependsOnMethods = "testStringPlugin", expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*implement Module.*")
    public void testWrongPlugin2() throws Exception {
        builder
            .withPluginDir(lookupPluginDir())
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.PluginWrong"));
    }

    @Test(dependsOnMethods = "testCustomPlugin")
    public void testConfiguredPlugin() throws Exception {
        Injector injector = builder
            .withPluginDir(lookupPluginDir())
            .withModuleBundle(bundle("impl", SpecificModule1.class))
            .withPluginBundle(bundle("plugin", ConfiguredModule.class))
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Key.get(String.class, Names.named("config"))), "b=[impl],m=[SpecificModule1{}],p=cscfg,c=ijcfg");
    }

    @Test(dependsOnMethods = "testCustomPlugin")
    public void testPluginProvider() throws Exception {
        Injector injector = builder
            .withPluginDir(lookupPluginDir())
            .withModuleBundle(bundle("impl", SpecificModule1.class))
            .withPluginBundle(bundle("plugin", ConfiguredProvider.class))
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Key.get(String.class, Names.named("config"))), "b=[impl],m=[SpecificModule1{}],p=cscfg,c=ijcfg");
    }

    @Test(dependsOnMethods = "testCustomPlugin")
    public void testPluginDirs() throws Exception {
        Injector injector = builder
            .withModuleBundle(Bundle.impl)
            .withPluginDir(lookupPluginDir(), false)
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.Plugin2"))
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "Foo2{}");
    }

    @Test(dependsOnMethods = "testPluginDirs")
    public void testPluginFlatDirs() throws Exception {
        Injector injector = builder
            .withModuleBundle(Bundle.impl)
            .withPluginDir(new File(lookupPluginDir(), "plugin2"), true)
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.Plugin2"))
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(CommonService.class).toString(), "Foo2{}");
    }

    @Test(dependsOnMethods = "testPluginDirs", expectedExceptions = IllegalArgumentException.class)
    public void testPluginFlatDirs2() throws Exception {
        builder
            .withModuleBundle(Bundle.impl)
            .withPluginDir(new File(lookupPluginDir(), "plugin2"), true)
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.Plugin1"));
    }

    @Test(dependsOnMethods = "testCustomPlugin")
    public void testClassLoader() throws Exception {
        ClassLoader classLoader = new URLClassLoader(new URL[] {}, this.getClass().getClassLoader());
        Injector injector = builder
            .withClassLoader(classLoader)
            .withPluginDir(lookupPluginDir())
            .withModuleBundle(Bundle.impl)
            .withPluginBundle(bundle("plugin", "com.maxifier.guice.bootstrap.Plugin1"))
            .buildApplicationInjector(Stage.DEVELOPMENT);
        assertEquals(injector.getInstance(Foo.class).getClass().getClassLoader().getParent(), classLoader);
        assertEquals(injector.getInstance(CommonService.class).getClass().getClassLoader(), classLoader.getParent());
    }

    @Test(dependsOnMethods = "testPluginNoConfig")
    public void testMultibinder() throws Exception {
        Injector injector = new InjectorBuilder()
            .withModule(new Multibinder1())
            .withPlugin(new Multibinder2())
            .buildApplicationInjector(Stage.DEVELOPMENT);
        Object[] set = ((Set<?>) injector.getInstance(Key.get(Types.setOf(Foo.class)))).toArray();
        Arrays.sort(set, Ordering.usingToString());
        assertEquals(Arrays.toString(set), "[FooImpl2{}, FooImplEx{}]");
    }

    private static ModuleBundle bundle(final String name, final Class<?>... modules) {
        String[] moduleNames = new String[modules.length];
        for (int i = 0; i < modules.length; i++) {
            moduleNames[i] = modules[i].getName();
        }
        return bundle(name, moduleNames);
    }

    private static ModuleBundle bundle(final String name, final String... modules) {
        return new ModuleBundle() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Iterable<String> modules() {
                return asList(modules);
            }
        };
    }
}