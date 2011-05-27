package com.maxifier.guice.mbean;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

public final class MBeanModule extends AbstractModule {

    private final String domain;
    private final MBeanServer mbeanServer;


    /**
     * Make module where PlatfromMBeanServer will be used
     *
     * @param domain mbean domain
     * @return module to install
     */
    public static Module platform(String domain) {
        return new MBeanModule(domain, ManagementFactory.getPlatformMBeanServer());
    }

    /**
     * Make module of MBean integration
     *
     * @param domain      MBean domain
     * @param mbeanServer MBean Server which will be used
     * @return module to install
     */
    public static Module server(String domain, MBeanServer mbeanServer) {
        return new MBeanModule(domain, mbeanServer);
    }


    /**
     * Make the module with NOP MBeans.
     *
     * @return module to install
     */
    public static Module noOperations() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(MBeanManager.class).toInstance(MBeanManager.NO_OPERATIONS);
            }
        };
    }


    public MBeanModule(String domain, MBeanServer mbeanServer) {
        this.domain = domain;
        this.mbeanServer = mbeanServer;
    }

    @Override
    protected void configure() {
        bind(MBeanManager.class).toInstance(new MBeanManagerImpl(domain, mbeanServer, new CGLIBMBeanGenerator()));
        MBeanTypeListener listener = new MBeanTypeListener();
        requestInjection(listener);
        //noinspection unchecked
        bindListener(new AnnotationMatcher(MBean.class, com.magenta.guice.mbean.MBean.class), listener);
    }
}
