package com.maxifier.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import javax.management.*;

import static org.mockito.Mockito.*;

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
        MBeanServer server = mock(MBeanServer.class);
        when(server.registerMBean(any(), any(ObjectName.class))).thenAnswer(new Answer<Object>() {
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
        verify(server, times(3)).registerMBean(any(), any(ObjectName.class));
        verify(server).unregisterMBean(any(ObjectName.class));
    }

    @com.magenta.guice.mbean.MBean(name = "service=Foo")
    static class Foo implements FooMBean {
    }

    static interface FooMBean {
    }
}
