package com.maxifier.guice.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks method as called within database-enabled context.
 * <p>{@code @DB} annotation starts {@link UnitOfWork} before method call and ends it after call.</p>
 * <p>UnitOfWork state is taken into account by proxies generated by {@link DBEntityManagerProvider}. This proxies
 * create new EntityManager on a first request and closes them on UnitOfWork end.</p>
 * <p>Transactions are started automatically if transaction mode are {@code REQUIRED} or {@code REQUIRES_NEW}
 * and committed after last such method method exit. If transaction is active and annotated method ends
 * with exception than transaction marked as rollback only.</p>
 *
 * @author Aleksey Didik (23.05.2008 10:19:35)
 * @author Konstantin Lyamshin (2015-11-09 18:07)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DB {
    /**
     * Specifies what kind of db transaction handling should be used.<br/>
     * Available modes:
     * <ul>
     * <li>{@code NOT_REQUIRED} don't start JPA transaction at all, but uses already started transaction if exist.</li>
     * <li>{@code REQUIRES_NEW} use separate db connection for method handling in any case. Starts and commits
     * transaction on the connection automatically.</li>
     * <li>{@code REQUIRED} starts JPA transaction before method start if it isn't started yet.
     * Rollbacks transaction if method invocation ended with exception and commits otherwise.</li>
     * </ul>
     */
    Transaction transaction() default Transaction.NOT_REQUIRED;

    /**
     * Automatically retries marked method in case of {@link java.sql.SQLTransientException}.
     * <p>Use with extreme caution. Method retry may cause side effects with unpredictable results.</p>
     * <p>This functionality designed to cope with db deadlocks which are unavoidable in production environments.
     * This attribute provides only basic functionality. For more advanced cases handle database context manually.</p>
     * <p>Each next method call retry processed after some timeout. Retry stops by thread interruption.
     * Timeouts declared in {@link DBInterceptor#RETRY_TIMEOUTS}.</p>
     */
    int retries() default 0;

    enum Transaction {
        NOT_REQUIRED,
        REQUIRES_NEW,
        REQUIRED
    }
}
