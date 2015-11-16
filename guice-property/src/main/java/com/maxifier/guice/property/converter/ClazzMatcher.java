package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

/**
 * Project: Maxifier
 * Date: 28.10.2009
 * Time: 19:20:33
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ClazzMatcher extends AbstractMatcher<TypeLiteral<?>> {

    private final Class clazz;

    public ClazzMatcher(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        return clazz.isAssignableFrom(typeLiteral.getRawType());
    }
}
