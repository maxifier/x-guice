package com.magenta.guice.bootstrap.activator;

import com.google.inject.Singleton;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.List;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 25.02.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
@Singleton
public class ActivatorManagerImpl implements ActivatorManager {

    private final AnnotatedMethodCache methodCache = new AnnotatedMethodCache(Activate.class);
    private final List<Object> activators = new LinkedList<Object>();


    @Override
    public synchronized void register(Object activator) {
        activators.add(activator);
        methodCache.cache(activator.getClass());
    }

    @Override
    public synchronized void activate() {
        for (Object activator : activators) {
            Method[] methods = methodCache.get(activator.getClass());
            for (final Method method : methods) {
                if (method.getParameterTypes().length > 0) {
                    throw new IllegalStateException(String.format("Activator method '%s.%s' can't contains parameters",
                            activator.getClass(), method));
                }
                if (!method.isAccessible()) {
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        @Override
                        public Object run() {
                            method.setAccessible(true);
                            return null;
                        }
                    });
                }
                try {
                    method.invoke(activator);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Unable to invoke activator method %s.%s", activator.getClass(), method));
                }
            }
        }
        activators.clear();
    }
}
