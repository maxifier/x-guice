package com.magenta.guice.events;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

class ListenerReference<T> extends WeakReference<T> {
    private final ListenerClassInstance<T> listenerClass;

    public ListenerReference(ListenerClassInstance<T> listenerClass, T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
        this.listenerClass = listenerClass;
    }

    public synchronized void cleanUp() {
        listenerClass.remove(this);
    }
}
