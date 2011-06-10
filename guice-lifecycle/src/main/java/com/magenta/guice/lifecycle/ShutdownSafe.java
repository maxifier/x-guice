package com.magenta.guice.lifecycle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by: Aleksey Didik
 * Date: 6/7/11
 * Time: 5:34 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ShutdownSafe {
    //int timeout() default 0;
}
