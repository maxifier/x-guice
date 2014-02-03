package com.magenta.guice.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.06.2009
 * Time: 16:48:33
 */
class HandlerMethodInstance<T> implements HandlerMethodInfo {
    private static final Logger LOG = LoggerFactory.getLogger(HandlerMethodInstance.class);

    private final HandlerMethod<T> method;
    private final ListenerClassInstance<T> listenerClass;

    private int matcherInvocations = 0;
    private int methodInvocations = 0;

    public HandlerMethodInstance(HandlerMethod<T> method, ListenerClassInstance<T> listenerClass) {
        this.method = method;
        this.listenerClass = listenerClass;
    }

    public boolean invokeIfMatched(Object event) {
        matcherInvocations++;
        return method.isMatched(event) && listenerClass.invokeHandler(this, event);
    }

    public void invokeHandler(T listener, Object o) {
        methodInvocations++;
        try {
            method.invokeHandler(listener, o);
        } catch (Throwable e) {
            LOG.warn("Unhandled exception in handler " + method, e);
        }
    }

    public boolean checkClass(Class c) {
        return method.checkClass(c);
    }

    @Override
    public String toString() {
        return method.toString();
    }

    @Override
    public Method getMethod() {
        return method.getMethod();
    }

    @Override
    public int getMatcherInvocations() {
        return matcherInvocations;
    }

    @Override
    public int getMethodInvocations() {
        return methodInvocations;
    }

    @Override
    public Class getListenerClass() {
        return listenerClass.getElementClass();
    }
}
