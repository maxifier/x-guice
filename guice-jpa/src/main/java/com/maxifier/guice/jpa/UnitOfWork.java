package com.maxifier.guice.jpa;

import com.maxifier.guice.jpa.DB.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * Class used for database context handling.
 * <p>Class uses thread-local state to handle database context propagation.</p>
 * <p>Database context can be handled manually. Use manual handling with caution, mismatch between
 * {@code begin()} and {@code end()} calls cause huge memory leaks.</p>
 * <p>Usage:</p>
 * <pre>
 * class Worker {
 *     &#64;Inject EntityManager em;
 *
 *     void doWork() {
 *         UnitOfWork.begin();
 *         try {
 *             em.find(Foo.class, 7);
 *         } finally {
 *             UnitOfWork.end();
 *         }
 *     }
 * }
 * </pre>
 * <p>Preferred for use in tests. Use {@link DBEntityManagerProvider} to create proper EM.</p>
 *
 * @author Konstantin Lyamshin (2015-11-09 18:22)
 */
public final class UnitOfWork {
    private static final Logger logger = LoggerFactory.getLogger(UnitOfWork.class);
    private static final ThreadLocal<UnitOfWork> current = new ThreadLocal<UnitOfWork>();
    private final UnitOfWork previous;
    private EntityManager entityManager;
    private boolean startTransaction;

    /**
     * Starts a new UnitOfWork for the current thread.
     * <p>UnitOfWork holds actual database context.</p>
     * <p>Always creates new UnitOfWork like in {@link Transaction#REQUIRES_NEW} mode. But don't start transaction
     * like in {@link Transaction#NOT_REQUIRED} mode. All transactions should be handled manually.</p>
     */
    public static void begin() {
        new UnitOfWork();
    }

    /**
     * Finishes current UnitOfWork.
     * <p>Current UnitOfWork must be previously started by {@link #begin()}.</p>
     */
    public static void end() {
        UnitOfWork context = current.get();
        if (context != null) {
            context.releaseConnection();
        } else {
            logger.error("Corrupted UnitOfWork call stack", new IllegalStateException());
        }
    }

    @Nullable
    static UnitOfWork get() {
        return current.get();
    }

    UnitOfWork() {
        this.previous = current.get();
        current.set(this);
    }

    boolean startTransaction() {
        if (startTransaction) {
            return false;
        }
        if (entityManager != null) {
            entityManager.getTransaction().begin();
        }
        startTransaction = true;
        return true;
    }

    void endTransaction() {
        if (entityManager != null) {
            EntityTransaction tr = entityManager.getTransaction();
            if (tr.getRollbackOnly()) {
                tr.rollback();
            } else {
                tr.commit();
            }
        }
        startTransaction = false;
    }

    void setRollbackOnly() {
        if (entityManager != null) {
            EntityTransaction tr = entityManager.getTransaction();
            if (tr.isActive()) {
                tr.setRollbackOnly();
            }
        }
    }

    EntityManager getConnection(EntityManagerFactory entityManagerFactory) {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            if (startTransaction) {
                entityManager.getTransaction().begin();
            }
        } else if (entityManager.getEntityManagerFactory() != entityManagerFactory) {
            throw new IllegalStateException("Multiple EntityManager instances not allowed within one DB context");
        }
        return entityManager;
    }

    void releaseConnection() {
        UnitOfWork context = current.get();
        if (context != this) {
            logger.error("Corrupted UnitOfWork call stack", new IllegalStateException());
        }

        // restore stack in-advance
        current.set(previous);

        if (entityManager == null) {
            return; // nothing to release
        }

        // exception-prone code

        EntityTransaction tr = entityManager.getTransaction();
        if (tr.isActive()) {
            logger.warn("Unfinished transaction found, trying to complete", new IllegalStateException());
            if (tr.getRollbackOnly()) {
                tr.rollback();
            } else {
                tr.commit();
            }
        }

        entityManager.close();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (entityManager != null) {
            sb.append("UnitOfWork{connected").append(startTransaction? ", transactional}": "}");
        } else if (startTransaction) {
            sb.append("UnitOfWork{transactional}");
        } else {
            sb.append("UnitOfWork{}");
        }
        return sb.toString();
    }
}
