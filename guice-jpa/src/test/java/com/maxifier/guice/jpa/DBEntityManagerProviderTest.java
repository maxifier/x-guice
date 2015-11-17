package com.maxifier.guice.jpa;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;

import static org.mockito.Mockito.*;

/**
 * @author Konstantin Lyamshin (2015-11-17 0:15)
 */
public class DBEntityManagerProviderTest extends org.testng.Assert {
    @Mock EntityManagerFactory emf;
    EntityManager proxy;

    @BeforeClass
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Metamodel metamodel = mock(Metamodel.class);
        when(emf.getMetamodel()).thenReturn(metamodel);
        proxy = new DBEntityManagerProvider(emf).get();
    }

    @Test
    public void testClose() throws Exception {
        proxy.close();
        verify(emf, never()).createEntityManager();
    }

    @Test
    public void testIsOpen() throws Exception {
        assertEquals(proxy.isOpen(), false);
        verify(emf, never()).createEntityManager();
    }

    @Test
    public void testEMF() throws Exception {
        EntityManagerFactory factory = proxy.getEntityManagerFactory();
        assertSame(factory, emf);
        verify(emf, never()).createEntityManager();
    }

    @Test
    public void testMetamodel() throws Exception {
        Metamodel metamodel = proxy.getMetamodel();
        assertSame(metamodel, emf.getMetamodel());
        verify(emf, never()).createEntityManager();
    }

    @Test
    public void testObjectMethods() throws Exception {
        assertEquals(proxy, proxy);
        assertEquals(proxy.hashCode(), System.identityHashCode(proxy));
        assertEquals(proxy.toString(), "EntityManagerProxy{}");
        verify(emf, never()).createEntityManager();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testNoContext() throws Exception {
        proxy.flush();
    }
}
