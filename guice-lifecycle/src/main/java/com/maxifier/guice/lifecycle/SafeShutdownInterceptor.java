package com.maxifier.guice.lifecycle;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by: Aleksey Didik
 * Date: 6/7/11
 * Time: 7:28 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
final class SafeShutdownInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SafeShutdownInterceptor.class);

    private final AtomicInteger free = new AtomicInteger();


    public SafeShutdownInterceptor() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                beforeShutdown();
                while (free.get() != 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
                afterShutdown();
            }
        });
    }

    void afterShutdown() {
        logger.info("Safe shutdown complete");

    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            free.getAndIncrement();
            return invocation.proceed();
        } finally {
            free.getAndDecrement();
        }
    }


    void beforeShutdown() {
        logger.info("Safe shutdown start...");
    }

}
