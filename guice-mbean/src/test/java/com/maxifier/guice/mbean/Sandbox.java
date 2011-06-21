package com.maxifier.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
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

    public static void main(String[] args) throws InterruptedException {
        Injector inj = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(MBeanModule.platform("test"));
                bind(Foo.class).asEagerSingleton();
                //bind interceptor to be sure
                bindInterceptor(Matchers.any(), Matchers.any(), new MethodInterceptor() {
                    @Override
                    public Object invoke(MethodInvocation invocation) throws Throwable {
                        return invocation.proceed();
                    }
                });
            }
        });

        Guice.createInjector(MBeanModule.server("<domain-name>", ManagementFactory.getPlatformMBeanServer()));
        Thread.sleep(100000000L);
    }

    @MBean(name = "service=Foo")
    public class Foo {

        private final MBeanManager mBeanManager;

        @Inject
        Foo(MBeanManager mBeanManager) {
            this.mBeanManager = mBeanManager;
        }

        public void make() {
            // mBeanManager.register("service=Foo Robots", new FooRobot());
        }
    }
}
