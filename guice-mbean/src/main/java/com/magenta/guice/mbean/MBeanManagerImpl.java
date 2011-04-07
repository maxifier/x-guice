package com.magenta.guice.mbean;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.management.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

@Singleton
public class MBeanManagerImpl implements MBeanManager {
    private static Logger logger = LoggerFactory.getLogger(MBeanManagerImpl.class);

    private static final String DOMAIN_NAME_DF = "yyyy/M/d-H.m.s";

    private final Collection<ObjectName> mbeans = new HashSet<ObjectName>();
    private final String domain;
    private final MBeanServer mbeanServer;

    public MBeanManagerImpl(String domain,
                            MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
        //add current time
        this.domain = domain + "-" + new SimpleDateFormat(DOMAIN_NAME_DF).format(new Date());
        addShutdownHook();
    }

    @Override
    public synchronized void register(Object... mbean) {
        if (mbean.length > 0) {
            register(Arrays.asList(mbean));
        }
    }

    @Override
    public void register(Iterable<Object> mbeans) {
        for (Object mbean : mbeans) {
            String name = resolveName(mbean);
            register(name, mbean);
        }
    }

    @Override
    public synchronized void register(String name, Object mbean) {
        try {
            ObjectName objectName = prepareObjectName(name);
            try {
                mbeanServer.registerMBean(mbean, objectName);
            } catch (InstanceAlreadyExistsException e) {
                logger.warn("Instance with name {} is already exists, second instance will be registered instead of first", name);
                try {
                    mbeanServer.unregisterMBean(objectName);
                } catch (InstanceNotFoundException e1) {
                    //NOP
                }
                mbeanServer.registerMBean(mbean, objectName);
            }
            mbeans.add(objectName);
        } catch (MalformedObjectNameException e) {
            logger.warn("Unable to register mbean, wrong name", e);
        } catch (MBeanRegistrationException e) {
            logger.warn("Unable to register mbean", e);
        } catch (NotCompliantMBeanException e) {
            logger.warn("Unable to register mbean, wrong mbean class", e);
        } catch (InstanceAlreadyExistsException e) {
            logger.warn("Unable to register mbean, instance already exists", e);
        }
    }

    @Override
    public synchronized void unregister(String... name) {
        if (name.length > 0) {
            unregister(Arrays.asList(name));
        }
    }

    @Override
    public void unregister(Iterable<String> names) {
        for (String name : names) {
            try {
                unregisterMBean(prepareObjectName(name));
            } catch (MalformedObjectNameException e) {
                logger.error("MBean unregistration error", e);
            }
        }
    }

    @PreDestroy
    //it's here for supply lifecycle
    public synchronized void unregisterAll() {
        //new set to prevent CME
        for (ObjectName objectName : new HashSet<ObjectName>(mbeans)) {
            unregisterMBean(objectName);
        }
    }

    String resolveName(Object mbean) {
        String name = null;
        Class<?> mbeanClass = mbean.getClass();
        MBean mbeanAnnotation = mbeanClass.getAnnotation(MBean.class);
        if (mbeanAnnotation != null) {
            name = mbeanAnnotation.name();
        }
        if (isBlank(name)) {
            name = makeDefaultName(mbeanClass);
        }
        return checkAlreadyDomained(name);
    }


    String checkAlreadyDomained(String mbeanName) {
        final int index = mbeanName.indexOf(':');
        if (index > -1) {
            logger.warn("Managed bean name already include domain." +
                    " MBeanManager domain '{}' will be used instead.", domain);
            return mbeanName.substring(index + 1);
        }
        return mbeanName;
    }

    String makeDefaultName(Class<?> mbeanClass) {
        return String.format("class=%s", mbeanClass.getName());
    }

    ObjectName prepareObjectName(String name) throws MalformedObjectNameException {
        return new ObjectName(String.format("%s:%s", domain, name));
    }

    private boolean isBlank(String name) {
        return name == null || name.trim().isEmpty();
    }

    private void unregisterMBean(ObjectName objectName) {
        try {
            mbeanServer.unregisterMBean(objectName);
        } catch (Exception e) {
            logger.warn("MBean unregistration error", e);
        }
        mbeans.remove(objectName);
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                unregister();
            }
        }));
    }
}

