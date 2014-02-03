package com.magenta.guice.events;


import java.lang.reflect.Method;
import java.util.List;

class HandlerMethod<T> {
    private final List<EventMatcher> matchers;
    private final List<EventClassMatcher> classMatchers;
    private final HandlerInvocator<Object, T> method;
    private final Class paramType;

    public HandlerMethod(List<EventMatcher> matchers, List<EventClassMatcher> classMatchers, HandlerInvocator<Object, T> method) {
        this.matchers = matchers;
        this.classMatchers = classMatchers;
        this.method = method;

        paramType = method.getParamType();
        if (classMatchers.isEmpty() && matchers.isEmpty() && paramType == null) {
            throw new RuntimeException("Handler " + method + " has neither filters nor parameter");
        }
    }

    public boolean checkClass(Class c) {
        if (paramType != null && !paramType.isAssignableFrom(c)) {
            return false;
        }
        for (EventClassMatcher matcher : classMatchers) {
            if (!matcher.matches(c)) {
                return false;
            }
        }
        return true;
    }

    public void invokeHandler(T listener, Object o) throws Exception {
        method.invoke(listener, o);
    }

    @SuppressWarnings({"unchecked"})
    public boolean isMatched(Object event) {
        for (EventMatcher matcher : matchers) {
            if (!matcher.matches(event)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "handler(" + method + ")";
    }

    public Method getMethod() {
        return method.getMethod();
    }
}
