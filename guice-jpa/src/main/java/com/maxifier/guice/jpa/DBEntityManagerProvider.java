package com.maxifier.guice.jpa;

import com.google.common.reflect.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Provides @DB context-sensitive {@code EntityManager} and handles it's methods calls.
 * <p>Create {@code EntityManager} proxy which delegates calls to actual EntityManages from current {@link UnitOfWork}.</p>
 * <p>Use {@code UnitOfWork} or {@code @DB} to initialize database context.</p>
 *
 * @author Konstantin Lyamshin (2015-11-15 21:12)
 */
@Singleton
public class DBEntityManagerProvider implements Provider<EntityManager>, InvocationHandler {
    private static final Method CLOSE = locateMethodIfExists("close");
    private static final Method IS_OPEN = locateMethodIfExists("isOpen");
    private static final Method GET_ENTITY_MANAGER_FACTORY = locateMethodIfExists("getEntityManagerFactory");
    private static final Method GET_METAMODEL = locateMethodIfExists("getMetamodel");
    private static final Logger logger = LoggerFactory.getLogger(DBEntityManagerProvider.class);
    private final EntityManagerFactory entityManagerFactory;
    private final EntityManager proxy;

    @Inject
    public DBEntityManagerProvider(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.proxy = Reflection.newProxy(EntityManager.class, this);
    }

    @Override
    public EntityManager get() {
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args); // Don't proxy Object's methods
        }

        if (method.equals(CLOSE)) {
            logger.warn("Try to manually close EntityManager", new UnsupportedOperationException());
            return null; // ignore
        }

        UnitOfWork context = UnitOfWork.get();
        if (context == null) {
            if (method.equals(IS_OPEN)) {
                return false; // no db context available
            }
            if (method.equals(GET_ENTITY_MANAGER_FACTORY)) {
                return entityManagerFactory; // EntityManagerFactory is always available
            }
            if (method.equals(GET_METAMODEL)) {
                return entityManagerFactory.getMetamodel(); // Meta model is always available
            }
            throw new IllegalStateException("No active DB context found use @DB or UnitOfWork to set it up");
        }

        EntityManager entityManager = context.getConnection(entityManagerFactory);

        // delegate to regular implementation
        return method.invoke(entityManager, args);
    }

    @Nullable
    private static Method locateMethodIfExists(String name, Class<?>... parameterTypes) {
        try {
            return EntityManager.class.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return o == this || o == proxy;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(proxy);
    }

    @Override
    public String toString() {
        UnitOfWork context = UnitOfWork.get();
        if (context != null) {
            return String.format("EntityManagerProxy{%s}", context);
        }
        return "EntityManagerProxy{}";
    }
}
