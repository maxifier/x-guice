package com.maxifier.guice.events;


public interface EventMatcher<T> {

    boolean matches(T event);
}

