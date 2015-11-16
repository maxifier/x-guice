package com.maxifier.guice.events;

import com.google.inject.Inject;
import com.maxifier.guice.mbean.MBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 02.11.2009
 * Time: 13:14:14
 */
@MBean(name = "service=EventDispatcher")
public class EventDispatcherControl implements EventDispatcherControlMBean {
    private final EventDispatcher eventDispatcher;

    @Inject
    public EventDispatcherControl(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public String showHandlersByEventClass() {
        return extractFromMap(eventDispatcher.getHandlersByEventClass());
    }

    private String extractFromMap(Map<Class, List<? extends HandlerMethodInfo>> m) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<Class, List<? extends HandlerMethodInfo>> entry : m.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                b.append(entry.getKey().getSimpleName()).append(": \n");
                for (HandlerMethodInfo handler : entry.getValue()) {
                    b.append("\t[matcher: ").append(handler.getMatcherInvocations()).append(", method: ").append(handler.getMethodInvocations()).append("] ");
                    Method method = handler.getMethod();
                    Annotation[] annotations = method.getAnnotations();
                    for (Annotation annotation : annotations) {
                        if (annotation.annotationType().equals(HandleClass.class)) {
                            HandleClass hc = (HandleClass) annotation;
                            b.append("@HandleClass(");
                            for (Class cls : hc.value()) {
                                b.append(cls.getSimpleName()).append(", ");
                            }
                            b.setLength(b.length() - 2);
                            b.append(") ");
                        } else if (!annotation.annotationType().equals(Handler.class)) {
                            String s = annotation.toString();
                            if (s.startsWith("@")) {
                                s = s.substring(1);
                            }
                            Class<? extends Annotation> at = annotation.annotationType();
                            String cn = at.getName();
                            if (s.startsWith(cn)) {
                                s = at.getSimpleName() + s.substring(cn.length());
                            }
                            b.append('@').append(s).append(' ');
                        }
                    }
                    b.append(method.getDeclaringClass().getSimpleName()).append('#').append(method.getName());
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        b.append("()");
                    } else {
                        b.append('(');
                        for (Class<?> parameterType : parameterTypes) {
                            b.append(parameterType.getSimpleName()).append(", ");
                        }
                        b.setLength(b.length() - 2);
                        b.append(')');
                    }
                    b.append('\n');
                }
            }
        }
        return b.toString();
    }

    @Override
    public String showHandlersByListenerClass() {
        return extractFromMap(eventDispatcher.getHandlersByListenerClass());
    }
}
