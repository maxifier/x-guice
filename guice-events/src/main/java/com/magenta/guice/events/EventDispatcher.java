package com.magenta.guice.events;

import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Диспетчер событий.
 */
@ImplementedBy(EventDispatcherImpl.class)
public interface EventDispatcher {
    void fireEvent(@NotNull Object event);

    /**
     * Добавляет слушателя. <b>Если в данный момент уже идет обработка какого-либо события, то данный метод возвращает
     * управление немедленно, но слушатель будет добавлен ТОЛЬКО после того, как все текущие события будут обработаны.
     *
     * @param o   слушатель
     * @param <T> тип слушателя
     */
    <T> void register(@NotNull T o);

    Map<Class, List<? extends HandlerMethodInfo>> getHandlersByEventClass();

    Map<Class, List<? extends HandlerMethodInfo>> getHandlersByListenerClass();
}
