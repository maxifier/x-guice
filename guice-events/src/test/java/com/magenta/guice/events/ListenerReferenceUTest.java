package com.magenta.guice.events;

import org.junit.Test;

import java.lang.ref.ReferenceQueue;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ListenerReferenceUTest {
    @Test
    public void testHash() throws Exception {
        Object handler1 = new Object();
        Object handler2 = new Object();

        ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();


        ListenerReference<Object> ref11 = new ListenerReference<Object>(null, handler1, referenceQueue);
        ListenerReference<Object> ref12 = new ListenerReference<Object>(null, handler1, referenceQueue);
        ListenerReference<Object> ref2 = new ListenerReference<Object>(null, handler2, referenceQueue);

        assertEquals(ref11, ref12);
        assertEquals(ref11.hashCode(), ref12.hashCode());

        assertThat(ref11.hashCode(), is(not(ref2.hashCode())));
    }
}