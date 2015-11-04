package com.maxifier.guice.events;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 02.11.2009
 * Time: 13:14:23
 */
public interface EventDispatcherControlMBean {
    String showHandlersByEventClass();

    String showHandlersByListenerClass();
}
