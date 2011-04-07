package com.magenta.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 19.01.2010 
* 
* Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public final class MBeanManagerModule extends AbstractModule {

    private final String domain;
    private final MBeanServer mbeanServer;

    public static Module platform(String domain) {
        return new MBeanManagerModule(domain, ManagementFactory.getPlatformMBeanServer());
    }

    public static Module server(String domain, MBeanServer mbeanServer) {
        return new MBeanManagerModule(domain, mbeanServer);
    }

    public static Module noOperations() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(MBeanManager.class).toInstance(MBeanManager.NO_OPERATIONS);
            }
        };
    }


    public MBeanManagerModule(String domain, MBeanServer mbeanServer) {
        this.domain = domain;
        this.mbeanServer = mbeanServer;
    }

    @Override
    protected void configure() {
        bind(MBeanManager.class).toInstance(new MBeanManagerImpl(domain, mbeanServer));
        MBeanTypeListener listener = new MBeanTypeListener();
        requestInjection(listener);
        bindListener(new MBeanAnnotationMatcher(), listener);
    }
}
