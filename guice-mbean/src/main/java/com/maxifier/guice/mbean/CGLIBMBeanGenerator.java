package com.maxifier.guice.mbean;


import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 7:41 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class CGLIBMBeanGenerator implements MBeanGenerator {

    private AnnotatedMethodCache methodCache = new AnnotatedMethodCache(com.magenta.guice.mbean.MBeanMethod.class, MBeanMethod.class);

    @Override
    public <T> T makeMBean(final T mbeanPretender) throws MBeanGenerationException {
        @SuppressWarnings({"unchecked"}) //just getClass feature
                Class<T> pretenderClass = (Class<T>) mbeanPretender.getClass();
        String pretenderClassName = pretenderClass.getName();
        String hash = Integer.toHexString(mbeanPretender.hashCode());
        final String mbeanName = pretenderClassName + "$$" + hash;
        final String mbeanInterfaceName = mbeanName + "MBean";

        Method[] methods = methodCache.get(pretenderClass);
        if (methods.length == 0) {
            throw new MBeanGenerationException("MBean pretender have no methods, annotated with @MBeanMethod");
        }
        InterfaceMaker interfaceMaker = new InterfaceMaker();
        interfaceMaker.setNamingPolicy(new NamingPolicy() {
            @Override
            public String getClassName(String prefix, String source, Object key, Predicate names) {
                return mbeanInterfaceName;
            }
        });
        for (Method method : methods) {
            interfaceMaker.add(method);
        }
        Class mbeanInterface = interfaceMaker.create();

        Enhancer enhancer = new Enhancer();
        enhancer.setNamingPolicy(new NamingPolicy() {
            @Override
            public String getClassName(String prefix, String source, Object key, Predicate names) {
                return mbeanName;
            }
        });
        enhancer.setSuperclass(pretenderClass);
        enhancer.setInterfaces(new Class[]{mbeanInterface});
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return method.invoke(mbeanPretender, args);
            }
        });
        //we know the result type
        //noinspection unchecked
        return (T) enhancer.create();
    }

}
