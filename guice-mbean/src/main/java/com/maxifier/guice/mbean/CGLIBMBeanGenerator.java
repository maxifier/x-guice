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

    @SuppressWarnings({"unchecked"})
    private AnnotatedMethodCache methodCache = new AnnotatedMethodCache(MBeanMethod.class);

    @Override
    public Object makeMBean(final Object mbeanPretender) throws MBeanGenerationException {
        Class<?> pretenderClass = mbeanPretender.getClass();
        String pretenderClassName = pretenderClass.getName();
        String hash = Integer.toHexString(mbeanPretender.hashCode());
        final String mbeanName = pretenderClassName + "$$" + hash;
        final String mbeanInterfaceName = mbeanName + "MBean";

        Method[] methods = methodCache.get(pretenderClass);
        if (methods.length == 0) {
            throw new MBeanGenerationException(String.format("MBean pretender %s have no methods, annotated with @MBeanMethod", pretenderClass.getName()));
        }
        InterfaceMaker interfaceMaker = new InterfaceMaker();
        interfaceMaker.setNamingPolicy(new DefinedNameNamingPolicy(mbeanInterfaceName));
        for (Method method : methods) {
            interfaceMaker.add(method);
        }
        Class mbeanInterface = interfaceMaker.create();

        Enhancer enhancer = new Enhancer();
        enhancer.setNamingPolicy(new DefinedNameNamingPolicy(mbeanName));
        //enhancer.setSuperclass(pretenderClass);
        enhancer.setInterfaces(new Class[]{mbeanInterface});
        enhancer.setCallback(new MBeanMethodInterceptor(mbeanPretender));
        //we know the result type
        //noinspection unchecked
        return enhancer.create();
    }

    private static class DefinedNameNamingPolicy implements NamingPolicy {
        private final String name;

        public DefinedNameNamingPolicy(String name) {
            this.name = name;
        }

        @Override
        public String getClassName(String prefix, String source, Object key, Predicate names) {
            return name;
        }
    }

    private static class MBeanMethodInterceptor implements MethodInterceptor {

        private final Object mbeanPretender;
        private final Class<?> mbeanPretenderClass;

        public MBeanMethodInterceptor(Object mbeanPretender) {
            this.mbeanPretender = mbeanPretender;
            this.mbeanPretenderClass = mbeanPretender.getClass();
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return mbeanPretenderClass.getMethod(method.getName(), method.getParameterTypes()).invoke(mbeanPretender, args);
        }
    }
}
