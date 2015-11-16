package com.maxifier.guice.events;

import com.google.inject.Singleton;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 27.06.2009
 * Time: 13:07:15
 */
@Singleton
public class ListenerRegistrationQueue {
    private final List<Object> awaiting = new LinkedList<Object>();

    private EventDispatcher dispatcher;

    public synchronized void register(Object o) {
        if (dispatcher == null) {
            awaiting.add(o);
        } else {
            dispatcher.register(o);
        }
    }

    synchronized void setDispatcher(EventDispatcher dispatcher) {
        if (this.dispatcher != null) {
            throw new RuntimeException("EventDispatcher is already set");
        }
        this.dispatcher = dispatcher;
        for(Object v : awaiting) {
            dispatcher.register(v);
        }
        awaiting.clear();
    }
}
