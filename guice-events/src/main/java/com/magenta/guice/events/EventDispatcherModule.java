package com.magenta.guice.events;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.06.2009
 * Time: 16:15:46
 */
public class EventDispatcherModule extends AbstractModule {
    @Override
    protected void configure() {
        final ListenerRegistrationQueue q = new ListenerRegistrationQueue();
        bind(ListenerRegistrationQueue.class).toInstance(q);
        bindListener(Matchers.any(), new RegistrationTypeListener(q));
        bind(EventDispatcherControl.class).asEagerSingleton();
    }

    public static void bind(Binder binder) {
        binder.install(new EventDispatcherModule());
    }

    private static class RegistrationTypeListener implements TypeListener {
        private final ListenerRegistrationQueue q;

        public RegistrationTypeListener(ListenerRegistrationQueue q) {
            this.q = q;
        }

        @Override
        public <I> void hear(TypeLiteral<I> iTypeLiteral, TypeEncounter<I> iTypeEncounter) {
            iTypeEncounter.register(new InjectionListener<I>() {
                @Override
                public void afterInjection(I i) {
                    if (i != null) {
                        q.register(i);
                    }
                }
            });
        }
    }
}
