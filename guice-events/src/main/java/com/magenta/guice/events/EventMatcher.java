package com.magenta.guice.events;


public interface EventMatcher<T> {

    boolean matches(T event);
}

