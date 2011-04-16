package com.maxifier.guice.scope;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 17:10:42
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ScopeAnnotation
public @interface UISingleton {
}
