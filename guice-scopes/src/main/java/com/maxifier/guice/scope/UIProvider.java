package com.maxifier.guice.scope;

import com.google.inject.Provider;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

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
