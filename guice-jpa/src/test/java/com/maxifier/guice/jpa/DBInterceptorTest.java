package com.maxifier.guice.jpa;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.lang.reflect.Field;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.maxifier.guice.jpa.DB.Transaction.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Konstantin Lyamshin (2015-11-16 23:54)
 */
@Guice(modules = {JPAModule.class, DBInterceptorTest.MockEMFModule.class})
public class DBInterceptorTest {
    @Inject EntityManagerFactory emf;
    @Inject EntityManager em;
    EntityManager emm;
    EntityTransaction etm;
    AtomicBoolean active;
    AtomicBoolean rollback;

    @BeforeMethod
    public void setUp() throws Exception {
        reset(emf);
        emm = mock(EntityManager.class);
        etm = mock(EntityTransaction.class);
        active = new AtomicBoolean();
        rollback = new AtomicBoolean();

        when(emf.createEntityManager()).thenReturn(emm);
        when(emm.getEntityManagerFactory()).thenReturn(emf);
        when(emm.getTransaction()).thenReturn(etm);

        when(etm.isActive()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return active.get();
            }
        });

        when(etm.getRollbackOnly()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return rollback.get();
            }
        });

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                active.set(true);
                return null;
            }
        }).when(etm).begin();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                active.set(false);
                rollback.set(false);
                return null;
            }
        }).when(etm).commit();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                active.set(false);
                rollback.set(false);
                return null;
            }
        }).when(etm).rollback();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                rollback.set(true);
                return null;
            }
        }).when(etm).setRollbackOnly();

        // Hack DBInterceptors internals to reduce timeouts significantly
        Field f = DBInterceptor.class.getDeclaredField("RETRY_TIMEOUTS");
        f.setAccessible(true);
        int[] dest = (int[]) f.get(null);
        System.arraycopy(new int[]{0, 100, 300, 5000}, 0, dest, 0, 4);
    }

    private static PersistenceException newTransientException() {
        return new PersistenceException(new SQLTransientException());
    }

    @Test
    public void testNoRetry() throws Exception {
        Callable<?> mock1 = mock(Callable.class);
        when(mock1.call()).thenReturn("OK");
        assertEquals(retry0(mock1), "OK");
        verify(mock1).call();

        Callable<?> mock2 = mock(Callable.class);
        when(mock2.call()).thenThrow(newTransientException());
        try { retry0(mock2); fail("Exception expected"); } catch (PersistenceException ignored) {}
        verify(mock2).call();
    }

    @Test
    public void testRetry() throws Exception {
        Callable<?> mock1 = mock(Callable.class);
        when(mock1.call()).thenReturn("OK1");
        assertEquals(retry2(mock1), "OK1");
        verify(mock1, times(1)).call();

        Callable<?> mock2 = mock(Callable.class);
        when(mock2.call())
            .thenThrow(newTransientException())
            .thenReturn("OK2");
        assertEquals(retry2(mock2), "OK2");
        verify(mock2, times(2)).call();

        Callable<?> mock3 = mock(Callable.class);
        when(mock3.call())
            .thenThrow(newTransientException())
            .thenThrow(newTransientException())
            .thenReturn("OK3");
        assertEquals(retry2(mock3), "OK3");
        verify(mock3, times(3)).call();

        Callable<?> mock4 = mock(Callable.class);
        when(mock4.call())
            .thenThrow(newTransientException())
            .thenThrow(newTransientException())
            .thenThrow(newTransientException())
            .thenReturn("OK4");
        try { retry2(mock4); fail("Exception expected"); } catch (PersistenceException ignored) {}
        verify(mock4, times(3)).call();
    }

    @Test
    public void testRetryTimeouts() throws Exception {
        final ArrayList<Long> ticks = new ArrayList<Long>();
        Callable<Void> recorder = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ticks.add(System.nanoTime());
                throw newTransientException();
            }
        };

        ticks.add(System.nanoTime());
        try { retry2(recorder); fail("Exception expected"); } catch (PersistenceException ignored) {}

        assertEquals(ticks.size(), 4);
        assertTrue(TimeUnit.NANOSECONDS.toMillis(ticks.get(1) - ticks.get(0)) >= 0); // in future
        assertTrue(TimeUnit.NANOSECONDS.toMillis(ticks.get(2) - ticks.get(1)) >= 100); // with timeout
        assertTrue(TimeUnit.NANOSECONDS.toMillis(ticks.get(3) - ticks.get(2)) >= 300); // with timeout
    }

    @Test
    public void testRetryInterrupt() throws Exception {
        Callable<?> mock = mock(Callable.class);
        when(mock.call()).thenThrow(newTransientException());

        Thread.currentThread().interrupt();
        try { retry2(mock); fail("Exception expected"); } catch (PersistenceException ignored) {}
        assertTrue(Thread.interrupted());
        verify(mock, times(1)).call();
    }

    @Test(expectedExceptions = PersistenceException.class)
    public void testRetryNonTransient() throws Exception {
        Callable<?> mock = mock(Callable.class);
        when(mock.call())
            .thenThrow(new PersistenceException(new SQLNonTransientException()))
            .thenReturn("OK");
        retry2(mock);
    }

    @DB(retries = 0)
    Object retry0(Callable<?> callable) throws Exception {
        return callable.call();
    }

    @DB(retries = 2)
    Object retry2(Callable<?> callable) throws Exception {
        return callable.call();
    }

    @Test
    public void testNoConnection() throws Exception {
        dbNoConnection();
        verify(emf, never()).createEntityManager();
        verify(emm.getTransaction(), never()).begin();
        verify(emm.getTransaction(), never()).commit();
        verify(emm, never()).close();
    }

    @DB(transaction = REQUIRED)
    void dbNoConnection() {
        assertEquals(em.toString(), "EntityManagerProxy{UnitOfWork{transactional}}");
    }

    @Test
    public void testNoTransaction() throws Exception {
        dbNoTransaction();
        verify(emf).createEntityManager();
        verify(emm.getTransaction(), never()).begin();
        verify(emm.getTransaction(), never()).commit();
        verify(emm).close();
    }

    @DB(transaction = NOT_REQUIRED)
    void dbNoTransaction() {
        assertEquals(em.getTransaction().isActive(), false);
    }

    @Test
    public void testTransaction() throws Exception {
        dbTransaction();
        verify(emf).createEntityManager();
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).commit();
        verify(emm).close();
    }

    @DB(transaction = REQUIRED)
    void dbTransaction() {
        assertEquals(em.getTransaction().isActive(), true);
    }

    @Test
    public void testTransactionNew() throws Exception {
        dbTransactionNew();
        verify(emf).createEntityManager();
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).commit();
        verify(emm).close();
    }

    @DB(transaction = REQUIRES_NEW)
    void dbTransactionNew() {
        assertEquals(em.getTransaction().isActive(), true);
    }

    @Test(dependsOnMethods = "testNoTransaction")
    public void testNestedNoTransactions() throws Exception {
        dbNestedNoTransactions1();
        verify(emf).createEntityManager();
        verify(emm.getTransaction(), never()).begin();
        verify(emm.getTransaction(), never()).commit();
        verify(emm).close();
    }

    @DB(transaction = NOT_REQUIRED)
    void dbNestedNoTransactions1() {
        dbNestedNoTransactions2();
        assertEquals(em.getTransaction().isActive(), false);
    }

    @DB(transaction = NOT_REQUIRED)
    void dbNestedNoTransactions2() {
        assertEquals(em.getTransaction().isActive(), false);
    }

    @Test(dependsOnMethods = "testTransaction")
    public void testNestedTransactions() throws Exception {
        dbNestedTransactions1();
        verify(emf).createEntityManager();
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).commit();
        verify(emm).close();
    }

    @DB(transaction = REQUIRED)
    void dbNestedTransactions1() {
        dbNestedTransactions2();
        assertEquals(em.getTransaction().isActive(), true);
    }

    @DB(transaction = REQUIRED)
    void dbNestedTransactions2() {
        assertEquals(em.getTransaction().isActive(), true);
    }

    @Test(dependsOnMethods = "testTransactionNew")
    public void testNestedTransactionsNew() throws Exception {
        dbNestedTransactionsNew1();
        verify(emf, times(2)).createEntityManager();
        verify(emm.getTransaction(), times(2)).begin();
        verify(emm.getTransaction(), times(2)).commit();
        verify(emm, times(2)).close();
    }

    @DB(transaction = REQUIRES_NEW)
    void dbNestedTransactionsNew1() {
        assertEquals(em.getTransaction().isActive(), true);
        verify(emf, times(1)).createEntityManager();
        verify(emm.getTransaction(), times(1)).begin();
        dbNestedTransactionsNew2();
        verify(emm.getTransaction(), times(1)).commit();
        verify(emm, times(1)).close();
    }

    @DB(transaction = REQUIRES_NEW)
    void dbNestedTransactionsNew2() {
        assertEquals(em.getTransaction().isActive(), true);
        verify(emf, times(2)).createEntityManager();
        verify(emm.getTransaction(), times(2)).begin();
    }

    @Test(dependsOnMethods = "testTransaction")
    public void testStartTransaction() throws Exception {
        dbStartTransaction1();
        verify(emf).createEntityManager();
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).commit();
        verify(emm).close();
    }

    @DB(transaction = NOT_REQUIRED)
    void dbStartTransaction1() {
        assertEquals(em.getTransaction().isActive(), false);
        dbStartTransaction2();
        assertEquals(em.getTransaction().isActive(), false);
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).commit();
    }

    @DB(transaction = REQUIRED)
    void dbStartTransaction2() {
        assertEquals(em.getTransaction().isActive(), true);
    }

    @Test(dependsOnMethods = "testTransaction")
    public void testSupportsTransaction() throws Exception {
        dbSupportsTransaction1();
        verify(emf).createEntityManager();
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).commit();
        verify(emm).close();
    }

    @DB(transaction = REQUIRED)
    void dbSupportsTransaction1() {
        assertEquals(em.getTransaction().isActive(), true);
        dbSupportsTransaction2();
        assertEquals(em.getTransaction().isActive(), true);
    }

    @DB(transaction = NOT_REQUIRED)
    void dbSupportsTransaction2() {
        assertEquals(em.getTransaction().isActive(), true);
    }

    @Test(dependsOnMethods = "testNestedTransactionsNew")
    public void testDeepNesting() throws Exception {
        dbDeepNesting1();
        verify(emf, times(2)).createEntityManager();
        verify(emm.getTransaction(), times(2)).begin();
        verify(emm.getTransaction(), times(2)).commit();
        verify(emm, times(2)).close();
    }

    @DB(transaction = NOT_REQUIRED)
    void dbDeepNesting1() {
        assertEquals(em.getTransaction().isActive(), false);
        verify(emf, times(1)).createEntityManager();
        verify(emm.getTransaction(), times(0)).begin();
        verify(emm.getTransaction(), times(0)).commit();
        verify(emm, times(0)).close();
        dbDeepNesting2();
        assertEquals(em.getTransaction().isActive(), false);
    }

    @DB(transaction = REQUIRED)
    void dbDeepNesting2() {
        assertEquals(em.getTransaction().isActive(), true);
        dbDeepNesting3();
        active.set(true); // manually reset state because we have only one connection instance
        verify(emf, times(2)).createEntityManager();
        verify(emm.getTransaction(), times(2)).begin();
        verify(emm.getTransaction(), times(1)).commit();
        verify(emm, times(1)).close();
    }

    @DB(transaction = REQUIRES_NEW)
    void dbDeepNesting3() {
        assertEquals(em.getTransaction().isActive(), true);
        verify(emf, times(2)).createEntityManager();
        verify(emm.getTransaction(), times(2)).begin();
        verify(emm.getTransaction(), times(0)).commit();
        verify(emm, times(0)).close();
    }

    @Test
    public void testNoTransactionException() throws Exception {
        // ignore exceptions when no transaction is active
        dbNoTransactionException();
        verify(emm.getTransaction(), never()).begin();
        verify(emm.getTransaction(), never()).rollback();
    }

    @DB(transaction = NOT_REQUIRED)
    void dbNoTransactionException() {
        assertEquals(em.getTransaction().getRollbackOnly(), false);
        try { dbException(); fail("Exception expected"); } catch (PersistenceException ignored) {}
        assertEquals(em.getTransaction().getRollbackOnly(), false);
    }

    @Test(dependsOnMethods = "testTransaction")
    public void testTransactionException() throws Exception {
        // rollback transaction in case of exception
        dbTransactionException();
        verify(emm.getTransaction()).begin();
        verify(emm.getTransaction()).rollback();
    }

    @DB(transaction = REQUIRED)
    void dbTransactionException() {
        assertEquals(em.getTransaction().getRollbackOnly(), false);
        try { dbException(); fail("Exception expected"); } catch (PersistenceException ignored) {}
        assertEquals(em.getTransaction().getRollbackOnly(), true);
    }

    @DB(transaction = NOT_REQUIRED)
    void dbException() {
        assertEquals(em.getTransaction().getRollbackOnly(), false);
        throw new PersistenceException();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNoDB() throws Exception {
        em.flush();
    }

    static class MockEMFModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EntityManagerFactory.class).toInstance(mock(EntityManagerFactory.class));
        }
    }
}
