package com.maxifier.guice.mbean;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.util.Collection;
import java.util.HashSet;

/**
 * Project: Maxifier
 * Date: 17.08.2009
 * Time: 13:27:26
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public final class MBeanTypeListener implements TypeListener {

    private MBeanManager manager;
    private final Collection<Object> registrationQueue = new HashSet<Object>();

    @Override
    public <I> void hear(TypeLiteral<I> type, final TypeEncounter<I> encounter) {
        encounter.register(new MBeanInjectionListener<I>());
    }

    @Inject
    public void setManager(MBeanManager manager) {
        this.manager = manager;
        for (Object mbeans : registrationQueue) {
            manager.register(mbeans);
        }
        registrationQueue.clear();
    }

    class MBeanInjectionListener<I> implements InjectionListener<I> {
        public void afterInjection(I injectee) {
            if (manager != null) {
                manager.register(injectee);
            } else {
                registrationQueue.add(injectee);
            }
        }
    }
}
