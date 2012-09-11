package com.maxifier.guice.bootstrap.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.magenta.guice.bootstrap.xml.Alone;
import com.magenta.guice.bootstrap.xml.AsEager;
import com.magenta.guice.bootstrap.xml.Constant;
import com.magenta.guice.bootstrap.xml.Foo;
import com.magenta.guice.bootstrap.xml.In;
import com.magenta.guice.bootstrap.xml.InImpl;
import com.magenta.guice.bootstrap.xml.Second;
import com.magenta.guice.bootstrap.xml.TestAnnotation;
import com.magenta.guice.bootstrap.xml.TestInterface;
import com.magenta.guice.override.OverrideModule;
import groovy.lang.GroovyShell;
import org.junit.Test;

import java.io.InputStream;


public class GroovyModuleTest {


    @Test
    public void testGroovyModule() {
        String fileName = "groovy/test.groovy";
        InputStream resourceAsStream = GroovyModuleTest.class.getClassLoader().getResourceAsStream(fileName);
        GroovyShell shell = new GroovyShell();
        shell.setProperty("client", "forbes");
        GroovyModule gModule = new GroovyModule(resourceAsStream, shell);
        Injector inj = Guice.createInjector(OverrideModule.collect(gModule));
        //from FooModule
        inj.getInstance(Foo.class);
        //just component
        assertTrue(inj.getInstance(TestInterface.class) instanceof Second);
        //just component with annotation
        assertTrue(inj.getInstance(Key.get(TestInterface.class, TestAnnotation.class)) instanceof Second);
        //test constant
        inj.getInstance(Key.get(String.class, Constant.class));
        //test alone
        inj.getInstance(Alone.class);
        //test in SINGLETON scope
        In in1 = inj.getInstance(In.class);
        In in2 = inj.getInstance(In.class);
        assertTrue(in1 instanceof InImpl);
        assertTrue(in2 instanceof InImpl);
        assertTrue(in1 == in2);

        assertEquals(((InImpl) in1).getProperty(), "testValue");
        assertEquals(((InImpl) in1).getWeight(), 523.23, 0.001);

        //test asEager
        inj.getInstance(AsEager.class);
        //test constant
        inj.getInstance(Key.get(String.class, Names.named("test.name")));
    }


}
