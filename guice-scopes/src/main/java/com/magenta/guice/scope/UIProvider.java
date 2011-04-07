package com.magenta.guice.scope;

import com.google.inject.Provider;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

class UIProvider<T> implements Provider<T> {

    private Provider<T> provider;

    UIProvider(Provider<T> provider) {
        this.provider = provider;
    }

    @Override
    public T get() {
        if (SwingUtilities.isEventDispatchThread()) {
            return provider.get();
        } else {
            final Object[] t = new Object[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        t[0] = provider.get();
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            //noinspection unchecked
            return (T) t[0];
        }
    }

    @Override
    public String toString() {
        return "XScopes.UI_SINGLETON";
    }
}
