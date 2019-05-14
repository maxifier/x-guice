package com.maxifier.guice.events;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeUnit;

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

        Assert.assertEquals(ref11, ref12);
        Assert.assertEquals(ref11.hashCode(), ref12.hashCode());

        Assert.assertNotEquals(ref2.hashCode(), ref11.hashCode());
    }

    @Test
    public void referencedObjectGCed_hashCodeRemainsTheSame() throws Exception {
        Object o = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        ListenerReference<Object> ref = new ListenerReference<Object>(mock(ListenerClassInstance.class), o, referenceQueue);
        int hashBeforeGC = ref.hashCode();

        // actually this check is unnecessary we just want to ensure that object wasn't GCed too early.
        Assert.assertEquals(hashBeforeGC, System.identityHashCode(o), "Hash of reference should match hash of the object");
        // ensure the object can be GCed.
        //noinspection UnusedAssignment
        o = null;

        System.gc();
        Reference<?> r = referenceQueue.remove(TimeUnit.MINUTES.toMillis(1));
        if (r == null) {
            Assert.fail("GC should collect reference in 1 min");
        }
        Assert.assertSame(ref, r);

        Assert.assertNull(ref.get(), "Reference should be GCed");
        int hashAfterGC = ref.hashCode();
        Assert.assertEquals(hashAfterGC, hashBeforeGC, "Hash of ListenerReference should not change after referenced object was GCed");
    }

    @Test
    public void referencedObjectGCed_referencesBecomeUnequal() throws Exception {
        Object o = new Object();
        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
        ListenerReference<Object> ref1 = new ListenerReference<Object>(mock(ListenerClassInstance.class), o, referenceQueue);
        ListenerReference<Object> ref2 = new ListenerReference<Object>(mock(ListenerClassInstance.class), o, referenceQueue);

        // actually this check is unnecessary we just want to ensure that object wasn't GCed too early.
        Assert.assertEquals(ref1.hashCode(), System.identityHashCode(o), "Hash of reference should match hash of the object");

        Assert.assertEquals(ref2, ref1);

        // ensure the object can be GCed.
        //noinspection UnusedAssignment
        o = null;

        System.gc();
        Reference<?> r = referenceQueue.remove(TimeUnit.MINUTES.toMillis(1));
        if (r == null) {
            Assert.fail("GC should collect reference in 1 min");
        }
        Assert.assertNull(ref1.get(), "Reference should be GCed");
        Assert.assertNull(ref2.get(), "Reference should be GCed");

        Assert.assertTrue(!ref1.equals(ref2), "References should become unequal after referenced object was GCed");
    }
}
