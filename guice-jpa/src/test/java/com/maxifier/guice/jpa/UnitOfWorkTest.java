package com.maxifier.guice.jpa;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

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
        Mockito.when(emf.createEntityManager()).thenReturn(em);
        Mockito.when(em.getTransaction()).thenReturn(tr);
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
        UnitOfWork context1 = UnitOfWork.create();
        assertSame(context1.getConnection(emf), em);
        Mockito.verify(emf, Mockito.times(1)).createEntityManager();
        Mockito.verify(em, Mockito.never()).getTransaction();
        assertSame(UnitOfWork.get(), context1);

        // transactional
        UnitOfWork context2 = UnitOfWork.create();
        context2.startTransaction();
        assertSame(context2.getConnection(emf), em);
        Mockito.verify(emf, Mockito.times(2)).createEntityManager();
        Mockito.verify(em).getTransaction();
        Mockito.verify(tr).begin();
        assertSame(UnitOfWork.get(), context2);

        // regular close
        context2.releaseConnection();
        Mockito.verify(em, Mockito.times(1)).close();
        Mockito.verify(tr, Mockito.times(1)).isActive();
        Mockito.verify(tr, Mockito.never()).commit();
        Mockito.verify(tr, Mockito.never()).rollback();
        assertSame(UnitOfWork.get(), context1);

        // orphan transaction close
        Mockito.when(tr.isActive()).thenReturn(true);
        context1.releaseConnection();
        Mockito.verify(em, Mockito.times(2)).close();
        Mockito.verify(tr, Mockito.times(2)).isActive();
        Mockito.verify(tr).commit();
        assertSame(UnitOfWork.get(), null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testMismatchEMF() throws Exception {
        UnitOfWork context = UnitOfWork.create();
        context.getConnection(emf);
        context.getConnection(Mockito.mock(EntityManagerFactory.class));
    }

    @Test
    public void testExceptionalRelease1() throws Exception {
        // non-transactional
        UnitOfWork context = UnitOfWork.create();
        context.getConnection(emf);
        assertSame(UnitOfWork.get(), context);

        Mockito.doThrow(new PersistenceException()).when(em).close();

        try { context.releaseConnection(); fail("Exception expected"); } catch (PersistenceException ignored) {}

        assertSame(UnitOfWork.get(), null);
    }

    @Test
    public void testExceptionalRelease2() throws Exception {
        // transactional
        UnitOfWork context = UnitOfWork.create();
        context.startTransaction();
        context.getConnection(emf);
        assertSame(UnitOfWork.get(), context);

        Mockito.when(tr.isActive()).thenReturn(true);
        Mockito.doThrow(new PersistenceException()).when(tr).commit();

        try { context.releaseConnection(); fail("Exception expected"); } catch (PersistenceException ignored) {}

        assertSame(UnitOfWork.get(), null);
    }

    @Test
    public void testTransactionNoConnection() throws Exception {
        UnitOfWork context = UnitOfWork.create();
        assertEquals(context.toString(), "UnitOfWork{}");
        assertEquals(context.startTransaction(), true);
        assertEquals(context.startTransaction(), false);
        assertEquals(context.toString(), "UnitOfWork{transactional}");
        context.endTransaction();
        assertEquals(context.toString(), "UnitOfWork{}");
    }

    @Test
    public void testTransactionBeforeConnection() throws Exception {
        UnitOfWork context = UnitOfWork.create();
        assertEquals(context.toString(), "UnitOfWork{}");
        assertEquals(context.startTransaction(), true);
        assertEquals(context.toString(), "UnitOfWork{transactional}");
        assertEquals(context.startTransaction(), false);

        context.getConnection(emf);
        Mockito.verify(tr).begin();

        context.endTransaction();
        Mockito.verify(tr).commit();
        assertEquals(context.toString(), "UnitOfWork{connected}");
    }

    @Test
    public void testTransactionLater() throws Exception {
        UnitOfWork context = UnitOfWork.create();
        context.getConnection(emf);
        Mockito.verify(tr, Mockito.never()).begin();
        assertEquals(context.toString(), "UnitOfWork{connected}");

        assertEquals(context.startTransaction(), true);
        assertEquals(context.startTransaction(), false);
        assertEquals(context.toString(), "UnitOfWork{connected, transactional}");
        Mockito.verify(tr).begin();

        context.endTransaction();
        assertEquals(context.toString(), "UnitOfWork{connected}");
        Mockito.verify(tr).commit();
    }

    @Test
    public void testRollback() throws Exception {
        UnitOfWork context = UnitOfWork.create();
        context.startTransaction();
        context.getConnection(emf);

        Mockito.when(tr.isActive()).thenReturn(true);
        Mockito.when(tr.getRollbackOnly()).thenReturn(true);
        Mockito.verify(tr, Mockito.times(0)).rollback();

        context.endTransaction();
        Mockito.verify(tr, Mockito.times(1)).rollback();

        context.releaseConnection();
        Mockito.verify(tr, Mockito.times(2)).rollback();

        Mockito.verify(tr, Mockito.never()).commit();
    }

    @Test
    public void testSetRollbackOnly() throws Exception {
        UnitOfWork context = UnitOfWork.create();
        context.setRollbackOnly(); // nop, no connection

        context.getConnection(emf);
        context.setRollbackOnly(); // nop, no transaction
        Mockito.verify(tr, Mockito.never()).setRollbackOnly();

        Mockito.when(tr.isActive()).thenReturn(true);
        context.setRollbackOnly();
        Mockito.verify(tr).setRollbackOnly();
    }
}
