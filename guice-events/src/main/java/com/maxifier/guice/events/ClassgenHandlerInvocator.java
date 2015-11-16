package com.maxifier.guice.events;

import gnu.trove.map.hash.THashMap;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.reflect.Modifier.isPublic;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.06.2009
 * Time: 12:33:50
 */
public class ClassgenHandlerInvocator<T, L> extends ReflectionHandlerInvocator<T, L> {

    private static final Logger log = LoggerFactory.getLogger(ClassgenHandlerInvocator.class);

    private static final Map<Method, FastMethod> METHODS_CACHE = new THashMap<Method, FastMethod>();
    private static final Object[] NO_ARGS = {};

    private final FastMethod fastMethod;

    public ClassgenHandlerInvocator(Method method) {
        super(method);
        if (!isPublic(method.getDeclaringClass().getModifiers()) || !isPublic(method.getModifiers())) {
            fastMethod = null;
        } else {
            fastMethod = getOrCreateInvocator(method);
        }
    }

    private static synchronized FastMethod getOrCreateInvocator(Method method) {
        //noinspection unchecked
        FastMethod inv = METHODS_CACHE.get(method);
        if (inv == null) {
            try {
                inv = FastClass.create(method.getDeclaringClass()).getMethod(method);
                METHODS_CACHE.put(method, inv);
            } catch (Throwable e) {
                log.error("Unable to create FastMethod for " + method, e);
            }
        }
        return inv;
    }

    @Override
    public Object invoke(L instance, T message) throws Exception {
        if (fastMethod == null) {
            return super.invoke(instance, message);
        } else {
            if (paramType != null) {
                return fastMethod.invoke(instance, new Object[]{message});
            } else {
                return fastMethod.invoke(instance, NO_ARGS);
            }
        }
    }
}
