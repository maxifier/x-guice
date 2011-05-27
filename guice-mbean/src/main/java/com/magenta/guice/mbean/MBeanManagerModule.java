package com.magenta.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.maxifier.guice.mbean.MBeanModule;

import javax.management.MBeanServer;

/**
 * Old MBeanManager module
 */
@Deprecated
public final class MBeanManagerModule extends AbstractModule {

    private MBeanModule mBeanModule;

    @Deprecated
    public static Module platform(String domain) {
        return MBeanModule.platform(domain);
    }

    @Deprecated
    public static Module server(String domain, MBeanServer mbeanServer) {
        return MBeanModule.server(domain, mbeanServer);
    }

    @Deprecated
    public static Module noOperations() {
        return MBeanModule.noOperations();
    }

    public MBeanManagerModule(String domain, MBeanServer mbeanServer) {
        mBeanModule = new MBeanModule(domain, mbeanServer);
    }

    @Override
    protected void configure() {
        install(mBeanModule);
    }
}
