package com.magenta.guice.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
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
    public void fireEvent(@NotNull Object event) {
        boolean handled;
        synchronized (registrationQueue) {
            firingEvents++;
        }
        try {
            if (isLocked.get() == null) {
                // мы не должны дважды получать readLock в одном потоке. Поэтому получаем его ТОЛЬКО
                // если он не был получен ранее, и устанавливаем флаг
                readLock.lock();
                isLocked.set(Boolean.TRUE);
                try {
                    handled = fireEvent0(event);
                } finally {
                    // освободим блокировку и сбросим флажок блокировки
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
        boolean handled = false;
        Class c = event.getClass();
        List<HandlerMethodInstance> l = mapping.get(c);
        if (l == null) {
            readLock.unlock();
            writeLock.lock();
            try {
                l = mapping.get(c);
                if (l == null) {
                    l = new ArrayList<HandlerMethodInstance>();
                    mapping.put(c, l);
                    for (ListenerClassInstance<?> cls : classInfos.values()) {
                        cls.bindHandlers(l, c);
                    }
                }
            } finally {
                readLock.lock();
                writeLock.unlock();
            }
        }
        for (HandlerMethodInstance<?> method : l) {
            handled |= method.invokeIfMatched(event);
        }
        return handled;
    }

    /**
     * Этот метод вызывается, когда событие не было обработано. Реализация по умолчанию не делает ничего
     * (только выводит в лог warning).
     *
     * @param event событие.
     */
    protected void unhandledEvent(Object event) {
        LOG.warn("Event " + event + " of class " + event.getClass() + " was not processed");
    }

    @Override
    public final <T> void register(@NotNull T o) throws CyclicFilterAnnotationException {
        synchronized (registrationQueue) {
            if (firingEvents == 0) {
                // если никакие события не обрабатываются, зарегистрируем сразу (а новые события подождут)
                register0(o);
            } else {
                // если события уже обрабатываются, то просто добавим нашего слушателя в очередь
                registrationQueue.add(o);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> void register0(T o) {
        // этот метод всегда вызывается с блокировкой на registrationQueue и firingEvents = 0, и, таким образом,
        // не требует дополнительной синхронизации (он гарантированно будет выполняться в гордом одиночестве)
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
