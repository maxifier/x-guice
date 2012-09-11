package com.magenta.guice.bootstrap.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.junit.Test;

/**
 * Project: Maxifier
 * Date: 28.03.2008
 * Time: 8:57:43
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class XmlConfigurationTest {

    @Test
    public void testXmlComponent() {
        String fileName = "xml/test.xml";
        XmlModule xmlModule = new XmlModule(XmlConfigurationTest.class.getClassLoader().getResourceAsStream(fileName));
        Injector inj = Guice.createInjector(xmlModule);
        //from FooModule
        inj.getInstance(Foo.class);
        //just component
        assertTrue(inj.getInstance(TestInterface.class) instanceof First);
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

        //test asEager
        inj.getInstance(AsEager.class);
        //test constant
        inj.getInstance(Key.get(String.class, Names.named("test.name")));
    }


}
