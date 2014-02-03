package com.magenta.guice.events;

import com.google.inject.ImplementedBy;

import java.util.List;
import java.util.Map;

@ImplementedBy(EventDispatcherImpl.class)
public interface EventDispatcher {
    void fireEvent(Object event);

    <T> void register(T o);

    Map<Class, List<? extends HandlerMethodInfo>> getHandlersByEventClass();

    Map<Class, List<? extends HandlerMethodInfo>> getHandlersByListenerClass();
}
