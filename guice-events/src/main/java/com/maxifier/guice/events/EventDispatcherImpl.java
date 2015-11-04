package com.maxifier.guice.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Singleton
public class EventDispatcherImpl implements EventDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(EventDispatcherImpl.class);

    private final Map<Class, List<HandlerMethodInstance>> mapping;
    private final Map<Class, ListenerClassInstance> classInfos;
    private final Queue<Object> registrationQueue = new LinkedList<Object>();

    private final ReferenceQueue queue;

    private final Lock readLock;
    private final Lock writeLock;

    @SuppressWarnings({"unchecked"})
    @Inject
    public EventDispatcherImpl(ListenerRegistrationQueue q) {
        classInfos = new THashMap();
        mapping = new THashMap();
        queue = new ReferenceQueue();
        final Thread t = new Thread("EventDispatcher reference watcher thread") {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        ((ListenerReference<?>) queue.remove()).cleanUp();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();

        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();

        q.setDispatcher(this);
    }

    private final ThreadLocal<Boolean> isLocked = new ThreadLocal<Boolean>();
    private volatile int firingEvents;

    @Override
    public Map<Class, List<? extends HandlerMethodInfo>> getHandlersByEventClass() {
        // noinspection unchecked,RedundantCast
        return (Map) Collections.unmodifiableMap(mapping);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Map<Class, List<? extends HandlerMethodInfo>> getHandlersByListenerClass() {
        Map<Class, List<? extends HandlerMethodInfo>> res = new THashMap();
        for (Map.Entry<Class, ListenerClassInstance> entry : classInfos.entrySet()) {
            // noinspection unchecked
            res.put(entry.getKey(), entry.getValue().getHandlers());
        }
        return res;
    }

    @Override
    //NOSONAR
    public void fireEvent(Object event) {
        boolean handled;
        synchronized (registrationQueue) {
            firingEvents++;
        }
        try {
            if (isLocked.get() == null) {
                readLock.lock();
                isLocked.set(Boolean.TRUE);
                try {
                    handled = fireEvent0(event);
                } finally {
                    readLock.unlock();
                    isLocked.remove();
                }
            } else {
                handled = fireEvent0(event);
            }
        } finally {
            synchronized (registrationQueue) {
                firingEvents--;
                if (firingEvents == 0) {
                    while (!registrationQueue.isEmpty()) {
                        register0(registrationQueue.poll());
                    }
                }
            }
        }
        if (!handled) {
            unhandledEvent(event);
        }
    }

    //NOSONAR
    private boolean fireEvent0(Object event) {
        Class c = event.getClass();
        List<HandlerMethodInstance> l = getHandlerMethodInstances(c);
        boolean handled = false;
        for (HandlerMethodInstance<?> method : l) {
            handled |= method.invokeIfMatched(event);
        }
        return handled;
    }

    private List<HandlerMethodInstance> getHandlerMethodInstances(Class c) {
        List<HandlerMethodInstance> l = mapping.get(c);
        if (l == null) {
            readLock.unlock();
            if (writeLock.tryLock()) {
                try {
                    l = mapping.get(c);
                    if (l == null) {
                        l = getHandlerMethodInstances0(c);
                        mapping.put(c, l);
                    }
                } finally {
                    readLock.lock();
                    writeLock.unlock();
                }

            } else {
                readLock.lock();
                // https://jira.maxifier.com/browse/XGUICE-30
                //
                // If we can't acquire write lock, that might mean that another thread is holding it.
                //
                // Example of inverted stack trace:
                // Thread 1:
                //   -fireEvent  - holds readLock
                //    -fireEvent0
                //     -someHandler - tries to get lock X, but it has to wait for Thread 2.
                //
                // Thread 2:
                //   -someMethod - holds lock X
                //    -fireEvent    - holds readLock
                //     -fireEvent0  - releases readLock
                //             if event class never met before, it will waits for Thread 1 to acquire writeLock.
                //
                // In this case if we want to avoid deadlock, we will calculate list of methods, but not
                // store it anywhere. Anyway, if intensity of queries is not too high, the cache will be filled.
                l = getHandlerMethodInstances0(c);
            }
        }
        return l;
    }

    // This method doesn't do any caching, don't call it unless you know what you are doing.
    private List<HandlerMethodInstance> getHandlerMethodInstances0(Class c) {
        List<HandlerMethodInstance> res = new ArrayList<HandlerMethodInstance>();
        for (ListenerClassInstance<?> cls : classInfos.values()) {
            cls.bindHandlers(res, c);
        }
        return res;
    }

    protected void unhandledEvent(Object event) {
        LOG.warn("Event " + event + " of class " + event.getClass() + " was not processed");
    }

    @Override
    public final <T> void register(T o) throws CyclicFilterAnnotationException {
        synchronized (registrationQueue) {
            if (firingEvents == 0) {
                register0(o);
            } else {
                registrationQueue.add(o);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> void register0(T o) {
        Class<T> c = (Class<T>) o.getClass();
        //noinspection unchecked
        ListenerClassInstance<T> listenerClass = classInfos.get(c);
        if (listenerClass == null) {
            Class<?> currentClass = c;
            Set<Class<?>> classes = new THashSet();
            List<ListenerClass<?>> lc = new ArrayList<ListenerClass<?>>(classes.size());
            do {
                if (classes.add(currentClass)) {
                    ListenerClass<?> l = EventReflectionParser.getOrCreateClassInfo(currentClass);
                    if (l.hasHandlers()) {
                        lc.add(l);
                    }
                }
                for (Class<?> interf : currentClass.getInterfaces()) {
                    if (classes.add(interf)) {
                        ListenerClass<?> l = EventReflectionParser.getOrCreateClassInfo(interf);
                        if (l.hasHandlers()) {
                            lc.add(l);
                        }
                    }
                }
                currentClass = currentClass.getSuperclass();
            } while (currentClass != null);

            //noinspection unchecked
            listenerClass = new ListenerClassInstance<T>(c, queue, lc);
            listenerClass.bindHandlers(mapping);

            classInfos.put(c, listenerClass);
        }
        listenerClass.addListener(o);
    }

}
