package com.maxifier.guice.jpa;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.lang.reflect.Method;
import java.sql.SQLTransientException;
import java.util.Random;

import static com.maxifier.guice.jpa.DB.Transaction.NOT_REQUIRED;
import static com.maxifier.guice.jpa.DB.Transaction.REQUIRES_NEW;

/**
 * Intercepts methods marked by {@link DB @DB} and initialize database-enabled context.
 * <p>Database context held by {@link UnitOfWork} in thread local variable.</p>
 * <p>Use {@link DBEntityManagerProvider} to obtain context-sensitive {@code EntityManager} instances.</p>
 *
 * @author Konstantin Lyamshin (2015-11-15 23:25)
 */
public class DBInterceptor implements MethodInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(DBInterceptor.class);
    private static final Random RND = new Random();
    private static final int RETRY_TIMEOUTS[] = new int[]{
        0, 307, 2000, 11000, 19000, 31000, 53000, 89000, 151000, 241000, 307000
    };

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        // I don't care bridge methods so much because nested invocations cost almost nothing.
        // Significant resources spend on DB connect only, but in case of bridge methods connection
        // obtained only in the most deep invocation. In this case outer invocation became
        // connection and/or transaction owner and closes it properly.
        // In case of REQUIRES_NEW nested invocations create extra UnitOfWork, but
        // connection obtained only in the most deep one and almost no extra resources wasted.
        Method method = invocation.getMethod();
        DB config = method.getAnnotation(DB.class);
        if (config == null) {
            throw new IllegalStateException("@DB annotation not found on " + method);
        }

        // method retry processing
        int retries = 0;
        while (true) {
            try {
                return invoke0(config.transaction(), invocation);
            } catch (PersistenceException e) {
                if (e.getCause() instanceof SQLTransientException && retries++ < config.retries()) {
                    logger.error("Exception while @DB method processing retry #" + retries, e);
                    if (delayRetry(retries)) {
                        continue;
                    }
                }
                throw e;
            }
        }
    }

    private static boolean delayRetry(int n) {
        int maxN = RETRY_TIMEOUTS.length - 1;
        int timeout = n < maxN ? RETRY_TIMEOUTS[n] : RETRY_TIMEOUTS[maxN - 1] + RND.nextInt(RETRY_TIMEOUTS[maxN]);
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private Object invoke0(DB.Transaction transaction, MethodInvocation invocation) throws Throwable {
        UnitOfWork context = UnitOfWork.get();

        boolean connectionOwner = transaction == REQUIRES_NEW || context == null;
        if (connectionOwner) {
            context = UnitOfWork.create();
        }
        try {
            boolean transactionOwner = transaction != NOT_REQUIRED && context.startTransaction();
            try {
                return invocation.proceed();
            } catch (Exception e) {
                context.setRollbackOnly();
                throw e;
            } finally {
                if (transactionOwner) {
                    context.endTransaction();
                }
            }
        } finally {
            if (connectionOwner) {
                context.releaseConnection();
            }
        }
    }
}
