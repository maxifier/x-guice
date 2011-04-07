package com.magenta.guice.events;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.06.2009
 * Time: 11:44:47
 * <p/>
 * Обертка над методом с одним параметром или без параметров вообще.
 */
public abstract class HandlerInvocator<T, L> {
    protected final Method method;
    protected final Class paramType;

    public HandlerInvocator(Method method) {
        this.method = method;
        method.setAccessible(true);

        Class<?>[] pt = method.getParameterTypes();
        if (pt.length > 1) {
            throw new RuntimeException("Handler method couldn't have more than one parameter: " + method);
        }
        paramType = pt.length == 1 ? pt[0] : null;
    }

    public Class<T> getParamType() {
        //noinspection unchecked
        return paramType;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * @param instance экземпляр
     * @param t        параметр. Если метод не имеет параметров, то игнорируется
     * @return результат вызова, <code>null</code> если метод имеет тип void
     */
    public abstract Object invoke(L instance, T t);

    @Override
    public String toString() {
        return method.toString();
    }
}
