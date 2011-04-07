package com.magenta.guice.jpa;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInvocation;

import javax.persistence.EntityManager;

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
        EntityManagerProvider emp = mock(EntityManagerProvider.class);
        EntityManager em = mock(EntityManager.class);
        when(emp.getFromInterceptor()).thenReturn(new EntityManagerProvider.Info(em, true));
        DBInterceptor interceptor = new DBInterceptor();
        interceptor.setEntityManagerProvider(emp);
        interceptor.invoke(mi);
        //verify invocation
        verify(mi).proceed();
        //verify get EM
        verify(emp).getFromInterceptor();
        //verify close EM
        verify(em).close();
        //verify remove from EMP
        verify(emp).remove();
    }

    public void testSimpleNotOwner() throws Throwable {
        MethodInvocation mi = mock(MethodInvocation.class);
        when(mi.getMethod()).thenReturn(DbAnnotationTestClass.class.getMethod("testMethod"));
        EntityManagerProvider emp = mock(EntityManagerProvider.class);
        EntityManager em = mock(EntityManager.class);
        when(emp.getFromInterceptor()).thenReturn(new EntityManagerProvider.Info(em, false));
        DBInterceptor interceptor = new DBInterceptor();
        interceptor.setEntityManagerProvider(emp);
        interceptor.invoke(mi);
        //verify invocation
        verify(mi).proceed();
        //verify get EM
        verify(emp).getFromInterceptor();
        //verify not close EM
        verify(em, never()).close();
        //verify not remove from EMP
        verify(emp, never()).remove();
    }

    static class DbAnnotationTestClass {

        @DB
        public void testMethod() {
        }
    }
}
