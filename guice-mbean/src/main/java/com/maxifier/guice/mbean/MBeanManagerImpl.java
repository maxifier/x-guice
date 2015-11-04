package com.maxifier.guice.mbean;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.management.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Singleton
public class MBeanManagerImpl implements MBeanManager, com.magenta.guice.mbean.MBeanManager {
    private static Logger logger = LoggerFactory.getLogger(MBeanManagerImpl.class);

    private final Collection<ObjectName> mbeans = new HashSet<ObjectName>();
    private final String domain;
    private final MBeanGenerator mbeanGenerator;
    private final MBeanServer mbeanServer;

    public MBeanManagerImpl(String domain, MBeanServer mbeanServer, MBeanGenerator mBeanGenerator) {
        this.mbeanServer = mbeanServer;
        //add current time
        this.domain = domain;
        this.mbeanGenerator = mBeanGenerator;
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
            if (!checkCompliantion(mbean.getClass())) {
                try {
                    mbean = mbeanGenerator.makeMBean(mbean);
                } catch (MBeanGenerationException e) {
                    logger.warn(String.format("Unable to register mbean %s," +
                            " instance is not compliant with JMX spec and mbean generation has been failed", mbean), e);
                }
            }
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
            logger.warn(String.format("Unable to register %s mbean, wrong name", mbean), e);
        } catch (MBeanRegistrationException e) {
            logger.warn(String.format("Unable to register mbean %s", mbean), e);
        } catch (NotCompliantMBeanException e) {
            logger.warn(String.format("Unable to register mbean %s, wrong mbean class", mbean), e);
        } catch (InstanceAlreadyExistsException e) {
            logger.warn(String.format("Unable to register mbean %s, instance already exists", mbean), e);
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
        } else {
            //check old name
            com.magenta.guice.mbean.MBean oldAnnotation = mbeanClass.getAnnotation(com.magenta.guice.mbean.MBean.class);
            if (oldAnnotation != null) {
                name = oldAnnotation.name();
            }
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

    static boolean checkCompliantion(Class baseClass) {
        Class current = baseClass;
        Class mbeanInterface = null;
        while (current != null) {
            mbeanInterface =
                    findMBeanInterface(current, current.getName());
            if (mbeanInterface != null) break;
            current = current.getSuperclass();
        }
        return mbeanInterface != null;
    }

    private static Class findMBeanInterface(Class aClass, String aName) {
        Class current = aClass;
        while (current != null) {
            final Class[] interfaces = current.getInterfaces();
            final int len = interfaces.length;
            for (int i = 0; i < len; i++) {
                final Class inter =
                        implementsMBean(interfaces[i], aName);
                if (inter != null) return inter;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static Class implementsMBean(Class c, String clName) {
        String clMBeanName = clName + "MBean";
        if (c.getName().equals(clMBeanName)) {
            return c;
        }
        Class[] interfaces = c.getInterfaces();
        for (Class anInterface : interfaces) {
            if (anInterface.getName().equals(clMBeanName))
                return anInterface;
        }
        return null;
    }
}

