package com.maxifier.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 10:13 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class Sandbox {

    public static void main(String[] args) throws InterruptedException, MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        Foo object = new Foo();
        Object o = Enhancer.create(Foo.class, new NoOp() {
        });
        ManagementFactory.getPlatformMBeanServer().registerMBean(o, new ObjectName("test", "serice", "hello"));
    }

    public static class Foo implements FooMBean {
    }
    
    public interface FooMBean {}
}
