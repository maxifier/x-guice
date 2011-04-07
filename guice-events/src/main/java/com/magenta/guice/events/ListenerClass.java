package com.magenta.guice.events;

import java.util.List;

class ListenerClass<T> {
    private final Class<T> elementClass;
    private final List<HandlerMethod<T>> handlers;

    public ListenerClass(Class<T> elementClass, List<HandlerMethod<T>> m) {
        this.elementClass = elementClass;
        this.handlers = m;
    }

    public List<HandlerMethod<T>> getHandlers() {
        return handlers;
    }

    @Override
    public String toString() {
        return "Listener(" + elementClass + ", " + handlers.size() + " handlers)";
    }

    public boolean hasHandlers() {
        return !handlers.isEmpty();
    }
}
