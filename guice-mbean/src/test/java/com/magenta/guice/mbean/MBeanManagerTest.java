package com.magenta.guice.mbean;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import org.testng.annotations.*;

import javax.management.*;

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
@Test
public class MBeanManagerTest {

    private MBeanManagerImpl test;

    @BeforeTest
    public void makeTested() {
        test = new MBeanManagerImpl("test", mock(MBeanServer.class));
    }

    @Test
    public void testDefaultName() {
        final Object mbean = new Object();
        final String expected = String.format("class=java.lang.Object");
        final String result = test.resolveName(mbean);
        assertEquals(expected, result, "Default name is not right");
    }

    @Test
    public void testCheckAlreadyDomained() {
        String tested = "mockDomain:service=test";
        String result = test.checkAlreadyDomained(tested);
        assertEquals("service=test", result, "domain might be deleted");
    }

    @MBean(name = "service=test")
    static class Annotated {
    }

    @MBean
    static class AnnotatedNotNamed {
    }

    @Test
    public void testAnnotationName() {
        Object mbean = new Annotated();
        String expected = String.format("service=test");
        String result = test.resolveName(mbean);
        assertEquals(expected, result, "Name is caught from @MBean is not right");

        mbean = new AnnotatedNotNamed();
        expected = String.format("class=com.magenta.guice.mbean.MBeanManagerTest$AnnotatedNotNamed");
        result = test.resolveName(mbean);
        assertEquals(expected, result, "Name must be created by default");

    }


    @Test
    public void testUnregisterAll() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        test.register(new Object());
        test.register(new Annotated());
        test.unregister();
    }
}
