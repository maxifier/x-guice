package com.maxifier.guice.jpa;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;

import javax.persistence.EntityManager;

/**
 * Installs {@code @DB} annotation processor and {@code EntityManager} provider which supports automatic db-context handling.
 *
 * @author Konstantin Lyamshin (2015-11-15 23:19)
 */
public class JPAModule implements Module {
    @Override
    public void configure(Binder builder) {
        builder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(DB.class), new DBInterceptor());
        builder.bind(EntityManager.class).toProvider(DBEntityManagerProvider.class).in(Scopes.SINGLETON);
    }
}
