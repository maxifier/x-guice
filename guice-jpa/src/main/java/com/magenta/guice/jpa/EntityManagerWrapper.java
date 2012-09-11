package com.magenta.guice.jpa;

import com.google.inject.Inject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * @author aleksey.didik@maxifier.com (Aleksey Didik)
 */
class EntityManagerWrapper implements EntityManager {

    private final DBInterceptor dbInterceptor;

    @Inject
    EntityManagerWrapper(DBInterceptor dbInterceptor) {
        this.dbInterceptor = dbInterceptor;
    }

    EntityManager getEM() {
        EntityManager entityManager = dbInterceptor.current();
        if (entityManager == null) {
            throw new IllegalStateException("Entity Manager used in the method without @DB annotation.");
        }
        return entityManager;
    }

    @Override
    public void close() {
        //ignore programmer trying to close entity manager.
        //getEM().close();
    }


    @Override
    public void persist(Object entity) {
        getEM().persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        return getEM().merge(entity);
    }

    @Override
    public void remove(Object entity) {
        getEM().remove(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return getEM().find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, Map<String, Object> stringObjectMap) {
        return getEM().find(tClass, o, stringObjectMap);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType) {
        return getEM().find(tClass, o, lockModeType);
    }

    @Override
    public <T> T find(Class<T> tClass, Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        return getEM().find(tClass, o, lockModeType, stringObjectMap);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return getEM().getReference(entityClass, primaryKey);
    }

    @Override
    public void flush() {
        getEM().flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        getEM().setFlushMode(flushMode);
    }

    @Override
    public FlushModeType getFlushMode() {
        return getEM().getFlushMode();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        getEM().lock(entity, lockMode);
    }

    @Override
    public void lock(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        getEM().lock(o, lockModeType, stringObjectMap);
    }

    @Override
    public void refresh(Object entity) {
        getEM().refresh(entity);
    }

    @Override
    public void refresh(Object o, Map<String, Object> stringObjectMap) {
        getEM().refresh(o, stringObjectMap);
    }

    @Override
    public void refresh(Object o, LockModeType lockModeType) {
        getEM().refresh(o, lockModeType);
    }

    @Override
    public void refresh(Object o, LockModeType lockModeType, Map<String, Object> stringObjectMap) {
        getEM().refresh(o, lockModeType, stringObjectMap);
    }

    @Override
    public void clear() {
        getEM().clear();
    }

    @Override
    public void detach(Object o) {
        getEM().detach(o);
    }

    @Override
    public boolean contains(Object entity) {
        return getEM().contains(entity);
    }

    @Override
    public LockModeType getLockMode(Object o) {
        return getEM().getLockMode(o);
    }

    @Override
    public void setProperty(String s, Object o) {
        getEM().setProperty(s, o);
    }

    @Override
    public Map<String, Object> getProperties() {
        return getEM().getProperties();
    }

    @Override
    public Query createQuery(String ejbqlString) {
        return getEM().createQuery(ejbqlString);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> tCriteriaQuery) {
        return getEM().createQuery(tCriteriaQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String s, Class<T> tClass) {
        return getEM().createQuery(s, tClass);
    }

    @Override
    public Query createNamedQuery(String name) {
        return getEM().createNamedQuery(name);
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String s, Class<T> tClass) {
        return getEM().createNamedQuery(s, tClass);
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return getEM().createNativeQuery(sqlString);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return getEM().createNativeQuery(sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return getEM().createNativeQuery(sqlString, resultSetMapping);
    }

    @Override
    public void joinTransaction() {
        getEM().joinTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> tClass) {
        return getEM().unwrap(tClass);
    }

    @Override
    public Object getDelegate() {
        return getEM().getDelegate();
    }

    @Override
    public boolean isOpen() {
        return getEM().isOpen();
    }

    @Override
    public EntityTransaction getTransaction() {
        return getEM().getTransaction();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getEM().getEntityManagerFactory();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getEM().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return getEM().getMetamodel();
    }

}
