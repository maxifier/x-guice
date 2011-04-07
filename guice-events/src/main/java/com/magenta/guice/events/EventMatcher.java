package com.magenta.guice.events;

import org.jetbrains.annotations.NotNull;

public interface EventMatcher<T> {
    /**
     * @param event событие
     *
     * @return true, если событие должно быть обработано
     */
    boolean matches(@NotNull T event);
}

