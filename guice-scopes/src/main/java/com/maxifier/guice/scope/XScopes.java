package com.maxifier.guice.scope;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 17:08:34
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public final class XScopes {

    private XScopes() {
    }

    public static final Scope LAZY_SINGLETON = new Scope() {

        @Override
        public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
            return com.google.inject.Scopes.SINGLETON.scope(key, unscoped);
        }

        @Override
        public String toString() {
            return "MagentaScopes.LAZY_SINGLETON";
        }
    };

    public static final Scope THREAD_LOCAL = new Scope() {
        @Override
        public <T> Provider<T> scope(Key<T> key, final Provider<T> unscoped) {
            return new Provider<T>() {

                private final ThreadLocal<T> threadLocal = new ThreadLocal<T>();

                @Override
                public T get() {
                    T t = threadLocal.get();
                    if (t == null) {
                        t = unscoped.get();
                        threadLocal.set(t);
                    }
                    return t;
                }
            };
        }

        @Override
        public String toString() {
            return "MagentaScopes.THREAD_LOCAL";
        }
    };

    public static final Scope UI_SINGLETON = new Scope() {
        public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
            return new UIProvider<T>(com.google.inject.Scopes.SINGLETON.scope(key, unscoped));
        }
    };

    public static void bindScopes(Binder binder) {
        binder.bindScope(LazySingleton.class, LAZY_SINGLETON);
        binder.bindScope(UISingleton.class, UI_SINGLETON);
        binder.bindScope(ThreadLocalScope.class, THREAD_LOCAL);
    }
}
