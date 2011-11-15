package com.magenta.guice.events;

import org.jetbrains.annotations.NotNull;

public interface EventMatcher<T> {

    boolean matches(@NotNull T event);
}

