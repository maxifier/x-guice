package com.maxifier.guice.jpa;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import static org.mockito.Mockito.*;

/**
 * @author Konstantin Lyamshin (2015-11-17 0:49)
 */
public class UnitOfWorkTest extends org.testng.Assert {
    @Mock EntityManagerFactory emf;
    @Mock EntityManager em;
    @Mock EntityTransaction tr;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(tr);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        // cleanup thread-locals
        while (UnitOfWork.get() != null) {
            UnitOfWork.end();
        }
    }

    @Test
    public void testManualContext() throws Exception {
        assertNull(UnitOfWork.get());

        UnitOfWork.begin();
        assertNotNull(UnitOfWork.get());

        UnitOfWork.begin();
        assertNotNull(UnitOfWork.get());

        UnitOfWork.end();
        assertNotNull(UnitOfWork.get());

        UnitOfWork.end();
        assertNull(UnitOfWork.get());
    }

    @Test
    public void testConnect() throws Exception {
        // non-transactional
        UnitOfWork context1 = new UnitOfWork();
        assertSame(context1.getConnection(emf), em);
        verify(emf, times(1)).createEntityManager();
        verify(em, never()).getTransaction();
        assertSame(UnitOfWork.get(), context1);

        // transactional
        UnitOfWork context2 = new UnitOfWork();
        context2.startTransaction();
        assertSame(context2.getConnection(emf), em);
        verify(emf, times(2)).createEntityManager();
        verify(em).getTransaction();
        verify(tr).begin();
        assertSame(UnitOfWork.get(), context2);

        // regular close
        context2.releaseConnection();
        verify(em, times(1)).close();
        verify(tr, times(1)).isActive();
        verify(tr, never()).commit();
        verify(tr, never()).rollback();
        assertSame(UnitOfWork.get(), context1);

        // orphan transaction close
        when(tr.isActive()).thenReturn(true);
        context1.releaseConnection();
        verify(em, times(2)).close();
        verify(tr, times(2)).isActive();
        verify(tr).commit();
        assertSame(UnitOfWork.get(), null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testMismatchEMF() throws Exception {
        UnitOfWork context = new UnitOfWork();
        context.getConnection(emf);
        context.getConnection(mock(EntityManagerFactory.class));
    }

    @Test
    public void testExceptionalRelease1() throws Exception {
        // non-transactional
        UnitOfWork context = new UnitOfWork();
        context.getConnection(emf);
        assertSame(UnitOfWork.get(), context);

        doThrow(new PersistenceException()).when(em).close();

        try { context.releaseConnection(); fail("Exception expected"); } catch (PersistenceException ignored) {}

        assertSame(UnitOfWork.get(), null);
    }

    @Test
    public void testExceptionalRelease2() throws Exception {
        // transactional
        UnitOfWork context = new UnitOfWork();
        context.startTransaction();
        context.getConnection(emf);
        assertSame(UnitOfWork.get(), context);

        when(tr.isActive()).thenReturn(true);
        doThrow(new PersistenceException()).when(tr).commit();

        try { context.releaseConnection(); fail("Exception expected"); } catch (PersistenceException ignored) {}

        assertSame(UnitOfWork.get(), null);
    }

    @Test
    public void testTransactionNoConnection() throws Exception {
        UnitOfWork context = new UnitOfWork();
        assertEquals(context.toString(), "UnitOfWork{}");
        assertEquals(context.startTransaction(), true);
        assertEquals(context.startTransaction(), false);
        assertEquals(context.toString(), "UnitOfWork{transactional}");
        context.endTransaction();
        assertEquals(context.toString(), "UnitOfWork{}");
    }

    @Test
    public void testTransactionBeforeConnection() throws Exception {
        UnitOfWork context = new UnitOfWork();
        assertEquals(context.toString(), "UnitOfWork{}");
        assertEquals(context.startTransaction(), true);
        assertEquals(context.toString(), "UnitOfWork{transactional}");
        assertEquals(context.startTransaction(), false);

        context.getConnection(emf);
        verify(tr).begin();

        context.endTransaction();
        verify(tr).commit();
        assertEquals(context.toString(), "UnitOfWork{connected}");
    }

    @Test
    public void testTransactionLater() throws Exception {
        UnitOfWork context = new UnitOfWork();
        context.getConnection(emf);
        verify(tr, never()).begin();
        assertEquals(context.toString(), "UnitOfWork{connected}");

        assertEquals(context.startTransaction(), true);
        assertEquals(context.startTransaction(), false);
        assertEquals(context.toString(), "UnitOfWork{connected, transactional}");
        verify(tr).begin();

        context.endTransaction();
        assertEquals(context.toString(), "UnitOfWork{connected}");
        verify(tr).commit();
    }

    @Test
    public void testRollback() throws Exception {
        UnitOfWork context = new UnitOfWork();
        context.startTransaction();
        context.getConnection(emf);

        when(tr.isActive()).thenReturn(true);
        when(tr.getRollbackOnly()).thenReturn(true);
        verify(tr, times(0)).rollback();

        context.endTransaction();
        verify(tr, times(1)).rollback();

        context.releaseConnection();
        verify(tr, times(2)).rollback();

        verify(tr, never()).commit();
    }

    @Test
    public void testSetRollbackOnly() throws Exception {
        UnitOfWork context = new UnitOfWork();
        context.setRollbackOnly(); // nop, no connection

        context.getConnection(emf);
        context.setRollbackOnly(); // nop, no transaction
        verify(tr, never()).setRollbackOnly();

        when(tr.isActive()).thenReturn(true);
        context.setRollbackOnly();
        verify(tr).setRollbackOnly();
    }
}
