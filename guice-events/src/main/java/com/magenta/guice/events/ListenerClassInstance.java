package com.magenta.guice.events;

import gnu.trove.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.06.2009
 * Time: 16:48:54
 */
class ListenerClassInstance<T> {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerClass.class);

    private final Class elementClass;

    @SuppressWarnings({"unchecked"})
    private final Set<ListenerReference<T>> listeners = new THashSet();
    private final List<HandlerMethodInstance<T>> handlers;
    private final ReferenceQueue<T> queue;
    private final Lock readLock;
    private final Lock writeLock;

    public ListenerClassInstance(Class elementClass, ReferenceQueue<T> queue, List<ListenerClass<?>> lc) {
        this.queue = queue;
        this.elementClass = elementClass;
        this.handlers = new ArrayList<HandlerMethodInstance<T>>();

        for (ListenerClass<?> listenerClass : lc) {
            for (HandlerMethod<?> method : listenerClass.getHandlers()) {
                //noinspection unchecked
                handlers.add(new HandlerMethodInstance<T>((HandlerMethod<T>) method, this));
            }
        }

        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();
    }

    public void remove(ListenerReference<T> ref) {
        if (!handlers.isEmpty()) {
            writeLock.lock();
            try {
                LOG.debug("Lost reference to {}", elementClass);
                listeners.remove(ref);
            } finally {
                writeLock.unlock();
            }
        }
    }


    public void addListener(final T listener) {
        if (!handlers.isEmpty()) {
            writeLock.lock();
            try {
                listeners.add(new ListenerReference<T>(this, listener, queue));
            } finally {
                writeLock.unlock();
            }
        }
    }

    public boolean invokeHandler(HandlerMethodInstance<T> method, Object event) {
        readLock.lock();
        try {
            boolean invoked = false;
            for (ListenerReference<T> listener : listeners) {
                final T l = listener.get();
                if (l != null) {
                    method.invokeHandler(l, event);
                    invoked = true;
                }
            }
            return invoked;
        } finally {
            readLock.unlock();
        }
    }

    public void bindHandlers(List<HandlerMethodInstance> dst, Class c) {
        for (HandlerMethodInstance handler : handlers) {
            if (handler.checkClass(c)) {
                dst.add(handler);
            }
        }
    }

    public void bindHandlers(Map<Class, List<HandlerMethodInstance>> mapping) {
        for (Map.Entry<Class, List<HandlerMethodInstance>> e : mapping.entrySet()) {
            Class c = e.getKey();
            List<HandlerMethodInstance> l = e.getValue();
            for (HandlerMethodInstance<T> handler : handlers) {
                if (handler.checkClass(c)) {
                    l.add(handler);
                }
            }
        }
    }

    public List<HandlerMethodInstance<T>> getHandlers() {
        return handlers;
    }

    @Override
    public String toString() {
        return "Listener(" + elementClass + ", " + handlers.size() + " handlers)";
    }

    public Class getElementClass() {
        return elementClass;
    }
}
