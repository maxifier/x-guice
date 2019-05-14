package com.maxifier.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Project: Maxifier
 * Date: 08.11.2009
 * Time: 14:55:33
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class DoubleStartTest {

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testDoubleStart() throws InterruptedException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException, InstanceNotFoundException {
        MBeanServer server = Mockito.mock(MBeanServer.class);
        Mockito.when(server.registerMBean(Mockito.any(), Mockito.any(ObjectName.class))).thenAnswer(new Answer<Object>() {
            int count = 0;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (++count != 2) {
                    return null;
                } else {
                    throw new InstanceAlreadyExistsException();
                }
            }
        });
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Foo.class).asEagerSingleton();
            }
        };
        Guice.createInjector(new MBeanModule("test", server), module);
        Guice.createInjector(new MBeanModule("test", server), module);
        Mockito.verify(server, Mockito.times(3)).registerMBean(Mockito.any(), Mockito.any(ObjectName.class));
        Mockito.verify(server).unregisterMBean(Mockito.any(ObjectName.class));
    }

    @MBean(name = "service=Foo")
    static class Foo implements FooMBean {
    }

    static interface FooMBean {
    }
}
