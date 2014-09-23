package com.magenta.guice.jpa;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 24.05.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
@SuppressWarnings("EntityManagerInspection")
public class DBInterceptorTest {

    @Mock
    private EntityManagerFactory defaultEMF;

    @Mock
    private EntityManagerFactory fooEMF;

    @Mock
    private EntityManagerFactory otherEMF;

    @Mock
    private EntityManagerFactory adjusterEMF;

    @Mock
    private EntityManager defaultEM;

    @Mock
    private EntityManager otherEM;

    @Mock
    private EntityManager fooEM;

    @Mock
    private EntityManager adjusterEM;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private EntityTransaction fooTransaction;

    @Mock
    private EntityTransaction adjusterTransaction;

    private Foo foo;
    private Injector injector;


    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(defaultEMF.createEntityManager()).thenReturn(defaultEM);
        when(fooEMF.createEntityManager()).thenReturn(fooEM);
        when(otherEMF.createEntityManager()).thenReturn(otherEM);
        when(adjusterEMF.createEntityManager()).thenReturn(adjusterEM);

        when(defaultEM.getTransaction()).thenReturn(transaction);
        when(fooEM.getTransaction()).thenReturn(fooTransaction);
        when(otherEM.getTransaction()).thenReturn(transaction);
        when(adjusterEM.getTransaction()).thenReturn(adjusterTransaction);

        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(EntityManagerFactory.class).toInstance(defaultEMF);
                bind(EntityManagerFactory.class).annotatedWith(Names.named("foo")).toInstance(fooEMF);
                bind(EntityManagerFactory.class).annotatedWith(Names.named("other")).toInstance(otherEMF);
                bind(EntityManagerFactory.class).annotatedWith(AdJuster.class).toInstance(adjusterEMF);
            }
        };

        injector = Guice.createInjector(Stage.PRODUCTION, module, new JPAModule());
        foo = injector.getInstance(Foo.class);
    }

    @Test
    public void testDefaultCall() throws Exception {
        foo.defaultDB();
        //verify
        verify(defaultEMF).createEntityManager();
        verify(adjusterEMF, never()).createEntityManager();
        verify(fooEMF, never()).createEntityManager();
        verify(otherEMF, never()).createEntityManager();
        verify(transaction, never()).begin();
        verify(transaction, never()).commit();
        verify(transaction, never()).rollback();
        verify(defaultEM).flush();
        verify(defaultEM).close();
    }

    @Test
    public void testDefaultTransactionalCall() throws Exception {
        foo.defaultTransactionalDB();
        //verify
        verify(defaultEMF).createEntityManager();
        verify(adjusterEMF, never()).createEntityManager();
        verify(fooEMF, never()).createEntityManager();
        verify(transaction).begin();
        verify(transaction).commit();
        verify(transaction, never()).rollback();
        verify(defaultEM).flush();
        verify(defaultEM).close();
    }

    @Test
    public void testDefaultTransactionalRollbackCall() throws Exception {
        try {
            foo.defaultTransactionalRollbackDB();
            fail("Error should not be caught");
        } catch (FooException ignore) {
        }
        //verify
        verify(defaultEMF).createEntityManager();
        verify(adjusterEMF, never()).createEntityManager();
        verify(fooEMF, never()).createEntityManager();
        verify(transaction).begin();
        verify(transaction, never()).commit();
        verify(transaction).rollback();
        verify(defaultEM).close();
    }

    @Test
    public void testAnnotatedByAnnotationCall() throws Exception {
        foo.fooDB();
        //verify
        verify(fooEMF).createEntityManager();
        verify(otherEMF, never()).createEntityManager();
        verify(defaultEMF, never()).createEntityManager();
        verify(adjusterEMF, never()).createEntityManager();
        verify(fooEM).flush();
        verify(fooEM).close();
    }

    @Test
    public void testAnnotatedByAnnotationClassCall() throws Exception {
        foo.adjusterDB();
        //verify
        verify(adjusterEMF).createEntityManager();
        verify(fooEMF, never()).createEntityManager();
        verify(defaultEMF, never()).createEntityManager();
        verify(adjusterEM).flush();
        verify(adjusterEM).close();
    }

    @Test
    public void testAnnotatedByAnnotationDoubleCall() throws Exception {
        foo.fooDoubleDB();
        //verify
        verify(fooEMF, times(1)).createEntityManager();
        verify(fooEM, times(1)).close();
    }

    @Test
    public void testAnnotatedByAnnotationClassDoubleCall() throws Exception {
        foo.adjusterDoubleDB();
        //verify
        verify(adjusterEMF, times(1)).createEntityManager();
        verify(adjusterEM, times(1)).close();
    }

    @Test
    public void testMatreshka() throws Exception {
        foo.matreshka();
        verify(defaultEMF, times(1)).createEntityManager();
        verify(defaultEM, times(1)).flush();
        verify(defaultEM, times(1)).close();
        verify(fooEMF, times(1)).createEntityManager();
        verify(fooEM, times(1)).flush();
        verify(fooEM, times(1)).close();
        verify(adjusterEMF, times(1)).createEntityManager();
        verify(adjusterEM, times(1)).flush();
        verify(adjusterEM, times(1)).close();
        verify(adjusterTransaction).begin();
        verify(adjusterTransaction).commit();
    }

    @Test
    public void testErrorMatreshka() throws Exception {
        try {
            foo.errorMatreshka();
            fail();
        } catch (FooException ignore) {
        }
        verify(defaultEMF, times(1)).createEntityManager();
        verify(defaultEM, times(1)).close();
        verify(fooEMF, times(1)).createEntityManager();
        verify(fooEM, times(1)).close();
        verify(adjusterEMF, times(1)).createEntityManager();
        verify(adjusterEM, times(1)).close();
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(fooTransaction).begin();
        verify(fooTransaction).rollback();
        verify(adjusterTransaction).begin();
        verify(adjusterTransaction).rollback();
    }

    @Test
    public void testMultiMatreshka() throws Exception {
        int threadsCount = 3;
        final CountDownLatch countDown = new CountDownLatch(threadsCount);
        Executor executor = Executors.newFixedThreadPool(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    foo.matreshka();
                    countDown.countDown();
                }
            });
        }
        countDown.await(2, TimeUnit.SECONDS);
        verify(defaultEMF, times(threadsCount)).createEntityManager();
        verify(defaultEM, times(threadsCount)).flush();
        verify(defaultEM, times(threadsCount)).close();
        verify(fooEMF, times(threadsCount)).createEntityManager();
        verify(fooEM, times(threadsCount)).flush();
        verify(fooEM, times(threadsCount)).close();
        verify(adjusterEMF, times(threadsCount)).createEntityManager();
        verify(adjusterEM, times(threadsCount)).flush();
        verify(adjusterEM, times(threadsCount)).close();
        verify(adjusterTransaction, times(threadsCount)).begin();
        verify(adjusterTransaction, times(threadsCount)).commit();
    }

    @Test
    public void testWithoutDB() throws Exception {
        try {
            foo.withoutDB();
            fail("IllegalStateException should bew here");
        } catch (IllegalStateException e) {
            //ok
        }
    }


    @Test
    public void testInheritance() throws Exception {
        Inherited instance = injector.getInstance(Inherited.class);
        instance.db();
        verify(defaultEMF).createEntityManager();
        verify(defaultEM).flush();
        verify(defaultEM).close();
    }

    @Test
    public void testWithInnerCall() throws Exception {
        EntityTransaction entityTransaction = new EntityTransaction() {

            boolean active = false;
            boolean committed = false;
            boolean rollbacked = false;

            @Override
            public void begin() {
                if (active) {
                    throw new RuntimeException("Already began");
                }
                active = true;

            }

            @Override
            public void commit() {
                if (committed || !active) {
                    throw new RuntimeException("Already committed or wasn't active");
                }
                active = false;
                committed = true;
            }

            @Override
            public void rollback() {
                if (rollbacked || !active) {
                    throw new RuntimeException("Already rollbacked or wasn't active");
                }
                rollbacked = true;
                active = false;
            }

            @Override
            public void setRollbackOnly() {
                rollbacked = true;
            }

            @Override
            public boolean getRollbackOnly() {
                return rollbacked;
            }

            @Override
            public boolean isActive() {
                return active;
            }
        };
        when(defaultEM.getTransaction()).thenReturn(entityTransaction);
        foo.withInnerCall();
        verify(defaultEMF, times(1)).createEntityManager();
        verify(defaultEM, times(2)).flush();
        verify(defaultEM, times(1)).close();
    }

    @Test
    public void testWithErrorInnerCall() throws Exception {
        when(transaction.isActive()).thenReturn(false, true);
        foo.withErrorInnerCall();
        verify(defaultEMF, times(1)).createEntityManager();
        verify(defaultEM, times(1)).flush();
        verify(defaultEM, times(1)).getDelegate();
        verify(defaultEM, times(1)).close();
        verify(transaction).setRollbackOnly();
    }

    @Test
    public void ConstructorCall() throws Exception {
        injector.getInstance(ConstructorCall.class);
        verify(defaultEMF).createEntityManager();
        verify(defaultEM).flush();
        verify(defaultEM).close();
    }

    @Test
    public void testNotPreparedDBInterceptor() throws Exception {
        DBInterceptor interceptor = new DBInterceptor();
        try {
            interceptor.invoke(null);
            fail("Should be unable to use unprepared interceptor.");
        } catch (IllegalStateException throwable) {
            //ok
        } catch (Throwable throwable) {
            throw new Exception(throwable);
        }
    }

    @Test
    public void testDoubleAnnotated() throws Exception {
        DBInterceptor dbInterceptor = new DBInterceptor();
        Method method = Bad.class.getDeclaredMethod("doubleAnnotated");
        try {
            dbInterceptor.getAnnotations(method);
            fail("Should be IllegalStateException because this method is double annotated");
        } catch (IllegalStateException e) {
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Test
    public void testNotRegisteredAnnotationDiscover() throws Exception {
        try {
            injector.getInstance(Bad.class);
            fail("Should not be create due to 2 errors!");
        } catch (ConfigurationException e) {
            Collection<Message> errorMessages = e.getErrorMessages();
            for (Message errorMessage : errorMessages) {
                assertTrue(errorMessage.getCause() instanceof IllegalStateException);
                assertTrue(errorMessage.getMessage().contains("At least two binding annotations used")
                        || errorMessage.getMessage().contains("Container contains @DB"));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testJustInTimeWrongAnnotations() throws Exception {
        Module module = new AbstractModule() {
            @Override
            protected void configure() {
                bind(EntityManagerFactory.class).annotatedWith(Names.named("foo")).toInstance(fooEMF);
                bind(EntityManagerFactory.class).annotatedWith(AdJuster.class).toInstance(adjusterEMF);
            }
        };

        injector = Guice.createInjector(Stage.DEVELOPMENT, module, new JPAModule());
        Bad instance = injector.getInstance(Bad.class);
        try {
            instance.doubleAnnotated();
            fail("Double annotated method should not be called");
        } catch (IllegalStateException ignored) {
        }
        try {
            instance.notRegisteredAnnotation();
            fail("Not registered annotation should not be served.");
        } catch (IllegalStateException ignored) {
        }
        try {
            instance.withUnexistedDefault();
            fail("Not registered annotation should not be served.");
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void testCloseIsNotClosing() throws Exception {
        foo.close();
        verify(defaultEM, times(1)).close();
    }


    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    @interface AdJuster {
    }

    static class Foo {

        private final EntityManager em;

        @Inject
        Foo(EntityManager em) {
            this.em = em;
        }

        @DB
        void defaultDB() {
            em.flush();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        void defaultTransactionalDB() {
            em.flush();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        void defaultTransactionalRollbackDB() {
            throw new FooException();
        }

        @DB
        @Named("foo")
        void fooDB() {
            em.flush();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        @Named("foo")
        void fooDBWithError() {
            throw new FooException();
        }

        @DB
        @Named("foo")
        void fooDoubleDB() {
            fooDB();
        }

        @DB
        @Named("other")
        void otherNamed() {
            em.flush();
        }

        @DB
        @AdJuster
        void adjusterDB() {
            em.flush();
        }

        @DB
        @AdJuster
        void adjusterDoubleDB() {
            adjusterDB();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        @AdJuster
        void adjusterThroughFooDB() {
            em.flush();
            fooDB();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        @AdJuster
        void adjusterThroughFooDBWithError() {
            em.flush();
            fooDBWithError();
        }

        @DB
        void matreshka() {
            em.flush();
            adjusterThroughFooDB();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        void errorMatreshka() {
            adjusterThroughFooDBWithError();

        }

        void withoutDB() {
            em.flush();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        void withInnerCall() {
            em.flush();
            innerCall();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        void withErrorInnerCall() {
            em.flush();
            try {
                innerErrorCall();
            } catch (FooException ignore) {
            }
            em.getDelegate();
        }

        @DB(transaction = DB.Transaction.REQUIRED)
        void innerCall() {
            em.flush();
        }

        @DB
        void innerErrorCall() {
            throw new FooException();
        }

        @DB
        public void close() {
            em.close();
        }
    }

    static class ConstructorCall {

        private final EntityManager em;

        @Inject
        ConstructorCall(EntityManager em) {
            this.em = em;
            db();
        }

        @DB
        void db() {
            em.flush();
        }
    }


    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    @interface Wrong {
    }

    static class Bad {

        @DB
        @Named("foo")
        @AdJuster
        void doubleAnnotated() {
        }

        @DB
        @Wrong
        void notRegisteredAnnotation() {

        }

        @DB
        void withUnexistedDefault() {

        }

    }

    static class Ancestor {

        private EntityManager em;

        Ancestor(EntityManager em) {
            this.em = em;
        }

        @DB
        void db() {
            em.flush();
        }
    }

    static class Inherited extends Ancestor {

        @Inject
        Inherited(EntityManager em) {
            super(em);
        }

    }

    static class FooException extends RuntimeException {
    }

}
