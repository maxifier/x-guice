package com.magenta.guice.events;

import java.lang.reflect.InvocationTargetException;
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
    }

    @Override
    public Object invoke(L instance, T message) {
        try {
            if (paramType != null) {
                return method.invoke(instance, message);
            } else {
                return method.invoke(instance);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
