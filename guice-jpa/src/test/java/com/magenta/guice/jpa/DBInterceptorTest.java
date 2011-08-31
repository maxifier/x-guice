package com.magenta.guice.jpa;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import static org.mockito.Mockito.*;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 24.05.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
public class DBInterceptorTest extends TestCase {


    public void testSimpleOwner() throws Throwable {
        MethodInvocation mi = mock(MethodInvocation.class);
        when(mi.getMethod()).thenReturn(DbAnnotationTestClass.class.getMethod("testMethod"));
        DBInterceptor interceptor = new DBInterceptor();
        EntityManagerFactory emf = mock(EntityManagerFactory.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction transaction = mock(EntityTransaction.class);
        when(em.getTransaction()).thenReturn(transaction);
        when(emf.createEntityManager()).thenReturn(em);
        interceptor.setEmf(emf);
        interceptor.invoke(mi);
        //verify invocation
        verify(mi).proceed();
        //verify get EM
        verify(em).close();
    }

    static class DbAnnotationTestClass {

        @DB
        public void testMethod() {
        }
    }
}
