package com.magenta.guice.jpa;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 24.05.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
public class DBInterceptor implements MethodInterceptor {

    private EntityManagerProvider entityManagerProvider;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        EntityManagerProvider.Info info = entityManagerProvider.getFromInterceptor();
        EntityManager entityManager = info.entityManager;
        boolean owner = info.firstlyAsked;

        DB dbAnnotation = invocation.getMethod().getAnnotation(DB.class);
        if (dbAnnotation == null) {
            throw new IllegalStateException("It's illegal state, this method must not be intercepted." +
                    " Use necessary matchers with this interceptors");
        }

        boolean inTransaction = dbAnnotation.transaction() != DB.Transaction.NOT_REQUIRED;

        boolean newTransaction = false;
        EntityTransaction transaction = null;
        if (inTransaction) {
            transaction = entityManager.getTransaction();
            newTransaction = !transaction.isActive(); //|| dbAnnotation.transaction() == DB.Transaction.REQUIRED_NEW;
        }

        try {
            if (inTransaction && newTransaction) {
                transaction.begin();
            }

            Object result = invocation.proceed();

            if (inTransaction && newTransaction) {
                transaction.commit();
            }

            return result;

        } catch (Throwable e) {
            if (inTransaction) {
                if (newTransaction) {
                    transaction.rollback();
                } else {
                    transaction.setRollbackOnly();
                }
            }
            throw e;
        }
        finally {
            if (owner) {
                entityManager.close();
                entityManagerProvider.remove();
            }
        }
    }

    @Inject
    public void setEntityManagerProvider(EntityManagerProvider entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    public static void bind(Binder binder) {
        DBInterceptor instance = new DBInterceptor();
        binder.requestInjection(instance);
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(DB.class), instance);
    }

}
