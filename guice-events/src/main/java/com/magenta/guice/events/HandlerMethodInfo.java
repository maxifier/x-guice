package com.magenta.guice.events;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 02.11.2009
 * Time: 13:20:17
 */
public interface HandlerMethodInfo {
    Class getListenerClass();

    Method getMethod();

    int getMatcherInvocations();

    int getMethodInvocations();
}
