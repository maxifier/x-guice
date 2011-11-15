package com.magenta.guice.events;

import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * ��������� �������.
 */
@ImplementedBy(EventDispatcherImpl.class)
public interface EventDispatcher {
    void fireEvent(@NotNull Object event);

    <T> void register(@NotNull T o);

    Map<Class, List<? extends HandlerMethodInfo>> getHandlersByEventClass();

    Map<Class, List<? extends HandlerMethodInfo>> getHandlersByListenerClass();
}
