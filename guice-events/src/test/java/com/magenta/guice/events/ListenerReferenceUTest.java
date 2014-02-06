package com.magenta.guice.events;

import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class ListenerReferenceUTest {
    @Test
    public void testHash() throws Exception {
        Object handler1 = new Object();
        Object handler2 = new Object();

        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();


        ListenerReference<Object> ref11 = new ListenerReference<Object>(mock(ListenerClassInstance.class), handler1, referenceQueue);
        ListenerReference<Object> ref12 = new ListenerReference<Object>(mock(ListenerClassInstance.class), handler1, referenceQueue);
        ListenerReference<Object> ref2 = new ListenerReference<Object>(mock(ListenerClassInstance.class), handler2, referenceQueue);

        assertEquals(ref11, ref12);
        assertEquals(ref11.hashCode(), ref12.hashCode());

        assertThat(ref11.hashCode(), is(not(ref2.hashCode())));
    }

    @Test
    public void referencedObjectGCed_hashCodeRemainsTheSame() throws Exception {
        Object o = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        ListenerReference<Object> ref = new ListenerReference<Object>(mock(ListenerClassInstance.class), o, referenceQueue);
        int hashBeforeGC = ref.hashCode();

        // actually this check is unnecessary we just want to ensure that object wasn't GCed too early.
        assertEquals("Hash of reference should match hash of the object", System.identityHashCode(o), hashBeforeGC);
        // ensure the object can be GCed.
        //noinspection UnusedAssignment
        o = null;

        System.gc();
        Reference<?> r = referenceQueue.remove(TimeUnit.MINUTES.toMillis(1));
        if (r == null) {
            fail("GC should collect reference in 1 min");
        }
        assertSame(ref, r);

        assertNull("Reference should be GCed", ref.get());
        int hashAfterGC = ref.hashCode();
        assertEquals("Hash of ListenerReference should not change after referenced object was GCed", hashBeforeGC, hashAfterGC);
    }

    @Test
    public void referencedObjectGCed_referencesBecomeUnequal() throws Exception {
        Object o = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        ListenerReference<Object> ref1 = new ListenerReference<Object>(mock(ListenerClassInstance.class), o, referenceQueue);
        ListenerReference<Object> ref2 = new ListenerReference<Object>(mock(ListenerClassInstance.class), o, referenceQueue);

        // actually this check is unnecessary we just want to ensure that object wasn't GCed too early.
        assertEquals("Hash of reference should match hash of the object", System.identityHashCode(o), ref1.hashCode());

        assertEquals(ref1, ref2);

        // ensure the object can be GCed.
        //noinspection UnusedAssignment
        o = null;

        System.gc();
        Reference<?> r = referenceQueue.remove(TimeUnit.MINUTES.toMillis(1));
        if (r == null) {
            fail("GC should collect reference in 1 min");
        }
        assertNull("Reference should be GCed", ref1.get());
        assertNull("Reference should be GCed", ref2.get());

        assertTrue("References should become unequal after referenced object was GCed", !ref1.equals(ref2));
    }
}