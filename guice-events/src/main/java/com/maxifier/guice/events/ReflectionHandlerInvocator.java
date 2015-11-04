package com.maxifier.guice.events;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.06.2009
 * Time: 12:33:50
 */
public class ReflectionHandlerInvocator<T, L> extends HandlerInvocator<T, L> {
    public ReflectionHandlerInvocator(Method method) {
        super(method);
        method.setAccessible(true);
    }

    @Override
    public Object invoke(L instance, T message) throws Exception {
        if (paramType != null) {
            return method.invoke(instance, message);
        } else {
            return method.invoke(instance);
        }
    }
}
