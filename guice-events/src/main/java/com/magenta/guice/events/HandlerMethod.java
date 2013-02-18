package com.magenta.guice.events;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

class HandlerMethod<T> {
    private final List<EventMatcher> matchers;
    private final List<EventClassMatcher> classMatchers;
    private final HandlerInvocator<Object, T> method;
    private final Class paramType;

    public HandlerMethod(@NotNull List<EventMatcher> matchers, @NotNull List<EventClassMatcher> classMatchers, @NotNull HandlerInvocator<Object, T> method) {
        this.matchers = matchers;
        this.classMatchers = classMatchers;
        this.method = method;

        paramType = method.getParamType();
        if (classMatchers.isEmpty() && matchers.isEmpty() && paramType == null) {
            throw new RuntimeException("Handler " + method + " has neither filters nor parameter");
        }
    }

    public boolean checkClass(@NotNull Class c) {
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

    public void invokeHandler(@NotNull T listener, @NotNull Object o) throws Exception {
        method.invoke(listener, o);
    }

    @SuppressWarnings({"unchecked"})
    public boolean isMatched(@NotNull Object event) {
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
