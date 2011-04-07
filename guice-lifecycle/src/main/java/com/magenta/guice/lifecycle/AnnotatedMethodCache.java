package com.magenta.guice.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for saving method-annotation association
 *
 * Project: X-Guice
 * Date: 10.09.2009
 * Time: 17:19:42
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
class AnnotatedMethodCache {
    private final Class<? extends Annotation>[] annotation;
    private static final int INITIAL_CAPACITY = 50;
    private final Map<Class<?>, Method[]> cache = new HashMap<Class<?>, Method[]>(INITIAL_CAPACITY);

    public AnnotatedMethodCache(Class<? extends Annotation>... annotation) {
        this.annotation = annotation;
    }

    public Method[] get(Class<?> key) {
        Method[] result = cache.get(key);
        if (result == null) {
            MethodArray methodArray = new MethodArray();
            //add all from super class
            get0(key, methodArray);
            result = methodArray.getArray();
            cache.put(key, result);
        }
        return result;
    }

    private void get0(Class<?> key, MethodArray methods) {
        if (key.equals(Object.class)) {
            return;
        }
        //from
        get0(key.getSuperclass(), methods);
        for (Method method : key.getDeclaredMethods()) {
            for (Class<? extends Annotation> aClass : annotation) {
                if (method.isAnnotationPresent(aClass)) {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    methods.addIfNotPresent(method);
                }
            }
        }
    }

    //brief copy from Class.class
    private static class MethodArray {
        private Method[] methods;
        private int length;

        MethodArray() {
            methods = new Method[5];
            length = 0;
        }

        void add(Method method) {
            if (length == methods.length) {
                methods = Arrays.copyOf(methods, methods.length + 2);
            }
            methods[length++] = method;
        }

        void addIfNotPresent(Method newMethod) {
            for (int i = 0; i < length; i++) {
                Method method = methods[i];
                if (isEqualByNameAndSignature(method, newMethod)) {
                    return;
                }
            }
            add(newMethod);
        }


        boolean isEqualByNameAndSignature(Method m1, Method m2) {
            return m1.getReturnType() == m2.getReturnType() &&
                    m1.getName().equals(m2.getName()) &&
                    isArrayContentsEq(m1.getParameterTypes(),
                            m2.getParameterTypes());
        }

        @SuppressWarnings({"ReturnOfCollectionOrArrayField"})
            //class used only from owner
        Method[] getArray() {
            compactAndTrim();
            return methods;
        }

        void compactAndTrim() {
            int newPos = 0;
            // Get rid of null slots
            for (int pos = 0; pos < length; pos++) {
                Method method = methods[pos];
                if (method != null) {
                    if (pos != newPos) {
                        methods[newPos] = method;
                    }
                    newPos++;
                }
            }
            if (newPos != methods.length) {
                methods = Arrays.copyOf(methods, newPos);
            }
        }

        boolean isArrayContentsEq(Object[] a1, Object[] a2) {
            if (a1 == null) {
                return a2 == null || a2.length == 0;
            }
            if (a2 == null) {
                return a1.length == 0;
            }
            if (a1.length != a2.length) {
                return false;
            }
            for (int i = 0; i < a1.length; i++) {
                if (!a1[i].equals(a2[i])) {
                    return false;
                }
            }
            return true;
        }

    }

    @Override
    public String toString() {
        return "AnnotatedMethodCache{" +
                "annotation=" + (annotation == null ? null : Arrays.asList(annotation)) +
                '}';
    }
}
