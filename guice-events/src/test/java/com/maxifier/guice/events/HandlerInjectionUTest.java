package com.maxifier.guice.events;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.06.2009
 * Time: 10:55:37
 */
public class HandlerInjectionUTest {
    private interface SomeService {
        @Handler
        void handle(String s);

        String whoAreYou();
    }

    static class SomeServiceImpl implements SomeService {
        final SomeService tracker = Mockito.mock(SomeService.class);

        @Override
        public void handle(String s) {
            tracker.handle(s);
        }

        @Override
        public String whoAreYou() {
            return tracker.whoAreYou();
        }
    }

    @Singleton
    static class MyEventDispatcher extends EventDispatcherImpl {
        @Inject
        public MyEventDispatcher(ListenerRegistrationQueue q, SomeService service) {
            super(q);
            assertEquals(service.whoAreYou(), "I am service");
        }
    }

    @Test
    public void testEventDispatcherInjection() {
        Injector inj = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new EventDispatcherModule());
                bind(EventDispatcher.class).to(MyEventDispatcher.class);
                SomeServiceImpl service = new SomeServiceImpl();
                bind(SomeService.class).toInstance(service);

                Mockito.when(service.tracker.whoAreYou()).thenReturn("I am service");
            }
        });

        SomeService someService = ((SomeServiceImpl) inj.getInstance(SomeService.class)).tracker;
        EventDispatcher eventDispatcher = inj.getInstance(EventDispatcher.class);

        eventDispatcher.fireEvent("test");

        Mockito.verify(someService).handle("test");
        Mockito.verify(someService).whoAreYou();
        Mockito.verifyNoMoreInteractions(someService);
    }

    @Test
    public void testInjection() {
        Injector inj = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new EventDispatcherModule());
                bind(EventDispatcher.class).to(EventDispatcherImpl.class);
                bind(AnimalListener.class).toInstance(new AnimalListenerWrapper());
            }
        });

        AnimalListener animalListener = ((AnimalListenerWrapper) inj.getInstance(AnimalListener.class)).tracker;
        EventDispatcher eventDispatcher = inj.getInstance(EventDispatcher.class);

        eventDispatcher.fireEvent(Animal.CAT);

        Mockito.verify(animalListener).animal(Animal.CAT);
        Mockito.verifyNoMoreInteractions(animalListener);
    }
}
