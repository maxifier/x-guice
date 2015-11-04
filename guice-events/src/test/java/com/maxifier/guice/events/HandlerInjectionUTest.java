package com.maxifier.guice.events;

import com.google.inject.*;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
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
                SomeService service = mock(SomeService.class);
                bind(SomeService.class).toInstance(service);

                when(service.whoAreYou()).thenReturn("I am service");
            }
        });

        SomeService someService = inj.getInstance(SomeService.class);
        EventDispatcher eventDispatcher = inj.getInstance(EventDispatcher.class);

        eventDispatcher.fireEvent("test");

        verify(someService).handle("test");
        verify(someService).whoAreYou();
        verifyNoMoreInteractions(someService);
    }

    @Test
    public void testInjection() {
        Injector inj = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new EventDispatcherModule());
                bind(EventDispatcher.class).to(EventDispatcherImpl.class);
                bind(AnimalListener.class).toInstance(mock(AnimalListener.class));
            }
        });

        AnimalListener animalListener = inj.getInstance(AnimalListener.class);
        EventDispatcher eventDispatcher = inj.getInstance(EventDispatcher.class);

        eventDispatcher.fireEvent(Animal.CAT);

        verify(animalListener).animal(Animal.CAT);
        verifyNoMoreInteractions(animalListener);
    }
}
