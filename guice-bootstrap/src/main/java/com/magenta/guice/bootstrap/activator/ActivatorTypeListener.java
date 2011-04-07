package com.magenta.guice.bootstrap.activator;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.util.Collection;
import java.util.HashSet;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 25.02.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
class ActivatorTypeListener implements TypeListener {

    private ActivatorManager manager;
    private final Collection<Object> registrationQueue = new HashSet<Object>();

    @Override
    public <I> void hear(TypeLiteral<I> type, final TypeEncounter<I> encounter) {
        encounter.register(new InjectionListener<I>() {
            public void afterInjection(I injectee) {
                synchronized (ActivatorTypeListener.this) {
                    if (manager != null) {
                        manager.register(injectee);
                    } else {
                        registrationQueue.add(injectee);
                    }
                }
            }
        });
    }

    @Inject
    public synchronized void setManager(ActivatorManager manager) {
        this.manager = manager;
        for (Object activators : registrationQueue) {
            manager.register(activators);
        }
        registrationQueue.clear();

    }
}
