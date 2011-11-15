package com.magenta.guice.events;

import org.testng.annotations.Test;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Test
public class EventDispatcherUTest {

    interface Listener {
        @Handler
        @HandleClass(Object.class)
        void test(String s);
    }

    @Test
    public void testMethodParam() {
        EventDispatcher dispatcher = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));
        Listener listener = mock(Listener.class);
        dispatcher.register(listener);
        dispatcher.fireEvent("123");

        verify(listener).test("123");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testClassFilters() {
        AnimalListener listener = mock(AnimalListener.class);
        EventDispatcher dispatcher = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));

        dispatcher.register(listener);

        for (Animal animal : Animal.values()) {
            dispatcher.fireEvent(animal);
            verify(listener).animal(animal);
        }

        verify(listener).dangerousAnimal(Animal.CROCODILE);
        verify(listener).dangerousAnimal(Animal.TIGER);
        verify(listener).eatableAnimal(Animal.RABBIT);

        verifyNoMoreInteractions(listener);
    }

    @Test(expectedExceptions = CyclicFilterAnnotationException.class)
    public void testCyclicAnnotations1() {
        class Test1 {
            @Handler
            @CyclicAnnotation1
            void test() {
            }
        }

        EventDispatcher d = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));
        d.register(new Test1());
    }

    @Test(expectedExceptions = CyclicFilterAnnotationException.class)
    public void testCyclicAnnotations2() {
        class Test2 {
            @Handler
            @CyclicAnnotation2
            void test() {
            }
        }

        EventDispatcher d = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));
        d.register(new Test2());
    }

    @Test(expectedExceptions = CyclicFilterAnnotationException.class)
    public void testAutoAnnotated() {
        class TestAuto {
            @Handler
            @InvalidAutoAnnotation
            void test() {
            }
        }

        EventDispatcher d = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));
        d.register(new TestAuto());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testEmptyHandler() {
        class Test1 {
            @Handler
            void test() {

            }
        }

        EventDispatcher d = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));
        d.register(new Test1());
    }

    @Test
    public void testGroupEvents() {
    }

    @Test
    public void testUnhandledEvents() {
        class TestEventDispatcher extends EventDispatcherImpl {
            boolean b;

            TestEventDispatcher() {
                super(mock(ListenerRegistrationQueue.class));
            }

            @Override
            protected synchronized void unhandledEvent(Object event) {
                assertFalse(b, "unhandled event passed twice");
                assertEquals(event, "event");
                b = true;
            }
        }
        TestEventDispatcher d = new TestEventDispatcher();
        d.fireEvent("event");
        assertTrue(d.b, "unhandled event wasn't processed");
    }

    @Test
    public void testWeakReference() throws Exception {
        class TestListener {
            boolean x = false;

            @Handler
            synchronized void test(Object o) {
                assertFalse(x);
                assertEquals(o, "123");
                x = true;
            }
        }

        ReferenceQueue<? super TestListener> q = new ReferenceQueue<TestListener>();
        EventDispatcher d = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));
        TestListener tl = new TestListener();

        WeakReference<TestListener> wr = new WeakReference<TestListener>(tl, q);
        d.register(tl);
        d.fireEvent("123");
        assertTrue(tl.x);

        //noinspection UnusedAssignment
        tl = null;

        System.gc();
        Thread.sleep(100);
        assertEquals(q.poll(), wr);
    }

    @Test
    public void testRegisterInHandler() {
        final EventDispatcher d = new EventDispatcherImpl(mock(ListenerRegistrationQueue.class));

        final Listener l = mock(Listener.class);

        class TestListener {
            int x = 0;

            @Handler
            synchronized void test(String s) {
                if (s.equals("123")) {
                    assertEquals(x, 0);
                    x++;
                    d.register(l);
                    d.fireEvent("321");
                } else if (s.equals("321")) {
                    assertEquals(x, 1);
                    x++;
                } else if (s.equals("222")) {
                    assertEquals(x, 2);
                    x++;
                } else {
                    fail("Strange event: " + s);
                }
            }
        }

        final TestListener tl = new TestListener();

        d.register(tl);

        d.fireEvent("123");
        d.fireEvent("222");

        assertEquals(tl.x, 3);
        verify(l).test("222");
        verifyNoMoreInteractions(l);
    }
}
