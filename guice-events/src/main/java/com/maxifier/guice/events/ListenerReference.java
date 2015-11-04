package com.maxifier.guice.events;

import javax.annotation.Nonnull;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

class ListenerReference<T> extends WeakReference<T> {
    private final ListenerClassInstance<T> listenerClass;
    private final int hash;

    public ListenerReference(@Nonnull ListenerClassInstance<T> listenerClass, @Nonnull T referent, @Nonnull ReferenceQueue<? super T> q) {
        super(referent, q);
        this.listenerClass = listenerClass;
        // identity hash code used, we don't want to rely on listener's hash code implementation.
        hash = System.identityHashCode(referent);
    }

    public synchronized void cleanUp() {
        listenerClass.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        // Even if the referenced object was GCed the reference is still equal to itself...
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListenerReference that = (ListenerReference) o;
        T t1 = get();
        Object t2 = that.get();
        // But after GC all references become unequal, even if they led to the same object before
        // we need this in order equals and hashCode to be consistent
        // We don't consider references equal after the object was GCed because hash code is preserved and objects
        // with difference hash code should not be equal.

        // Referential equality is used
        return t1 != null && t2 != null && t1 == t2;
    }

    @Override
    public int hashCode() {
        // preserve hash code even if the contained object is GCed
        // if hash code changes it may break hash sets
        return hash;
    }
}