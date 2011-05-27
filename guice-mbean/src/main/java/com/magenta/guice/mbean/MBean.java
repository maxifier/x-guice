package com.magenta.guice.mbean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Old annotation for mbeans
 *
 * @author Aleksey Didik
 * @deprecated use com.maxifier.guice.mbean.MBean instead
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MBean {
    String name() default "";
}
