package com.maxifier.guice.mbean;

import org.mockito.Matchers;
import org.testng.annotations.Test;

import javax.management.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

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

    @Test
    public void testDefaultName() {
        MBeanManagerImpl test = new MBeanManagerImpl("test", mock(MBeanServer.class), null);
        final Object mbean = new Object();
        final String expected = String.format("class=java.lang.Object");
        final String result = test.resolveName(mbean);
        assertEquals(expected, result, "Default name is not right");
    }

    @Test
    public void testCheckAlreadyDomained() {
        MBeanManagerImpl test = new MBeanManagerImpl("test", mock(MBeanServer.class), null);
        String tested = "mockDomain:service=test";
        String result = test.checkAlreadyDomained(tested);
        assertEquals("service=test", result, "domain might be deleted");
    }

    @com.magenta.guice.mbean.MBean(name = "service=test")
    static class OldAnnotated {
    }

    @com.magenta.guice.mbean.MBean
    static class OldAnnotatedNotNamed {
    }

    @MBean(name = "service=test")
    static class Annotated {
    }

    @MBean
    static class AnnotatedNotNamed {
    }

    @Test
    public void testAnnotationName() {
        MBeanManagerImpl test = new MBeanManagerImpl("test", mock(MBeanServer.class), null);
        Object mbean = new Annotated();
        String expected = String.format("service=test");
        String result = test.resolveName(mbean);
        assertEquals(expected, result, "Name is caught from @MBean is not right");

        mbean = new OldAnnotated();
        result = test.resolveName(mbean);
        assertEquals(expected, result, "Name is caught from @MBean is not right");


        mbean = new AnnotatedNotNamed();
        expected = String.format("class=com.maxifier.guice.mbean.MBeanManagerTest$AnnotatedNotNamed");
        result = test.resolveName(mbean);
        assertEquals(expected, result, "Name must be created by default");

        mbean = new OldAnnotatedNotNamed();
        expected = String.format("class=com.maxifier.guice.mbean.MBeanManagerTest$OldAnnotatedNotNamed");
        result = test.resolveName(mbean);
        assertEquals(expected, result, "Name must be created by default");
    }

    static interface FooCompliantMBean {
    }

    @MBean(name = "service=test")
    static class FooNotCompliant {
    }

    @MBean(name = "service=test")
    static class FooCompliant implements FooCompliantMBean {
    }

    @MBean(name = "service=test")
    static class FooCompliantToo extends FooCompliant {
    }


    @Test
    public void testCompliantion() throws Exception {
        assertTrue(MBeanManagerImpl.checkCompliantion(FooCompliant.class));
        assertTrue(MBeanManagerImpl.checkCompliantion(FooCompliantToo.class));
        assertFalse(MBeanManagerImpl.checkCompliantion(FooNotCompliant.class));
    }

    @Test
    public void testRegisterCompliant() throws Exception {
        MBeanGenerator mBeanGenerator = mock(MBeanGenerator.class);
        when(mBeanGenerator.makeMBean(Matchers.<Object>any())).thenReturn(new Object());
        MBeanManagerImpl test = new MBeanManagerImpl("test", mock(MBeanServer.class), mBeanGenerator);
        test.register(new FooCompliant());
        verifyZeroInteractions(mBeanGenerator);
    }

    @Test
    public void testRegisterNotCompliant() throws Exception {
        MBeanGenerator mBeanGenerator = mock(MBeanGenerator.class);
        when(mBeanGenerator.makeMBean(Matchers.<Object>any())).thenReturn(new Object());
        MBeanManagerImpl test = new MBeanManagerImpl("test", mock(MBeanServer.class), mBeanGenerator);
        FooNotCompliant fooNotCompliant = new FooNotCompliant();
        test.register(fooNotCompliant);
        verify(mBeanGenerator).makeMBean(fooNotCompliant);
    }


    @Test
    public void testUnregisterAll() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        MBeanManagerImpl test = new MBeanManagerImpl("test", mock(MBeanServer.class), mock(MBeanGenerator.class));
        test.register(new Object());
        test.register(new Annotated());
        test.unregister();
    }
}
