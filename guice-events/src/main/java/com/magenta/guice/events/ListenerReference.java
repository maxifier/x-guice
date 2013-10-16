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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListenerReference that = (ListenerReference) o;
        T t1 = get();
        Object t2 = that.get();
        return t1 == null ? t2 == null : t1.equals(t2);
    }

    @Override
    public int hashCode() {
        T t = get();
        return t == null ? 0 : t.hashCode();
    }
}
