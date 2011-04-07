package com.magenta.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.internal.util.ToStringBuilder;
import com.google.inject.matcher.AbstractMatcher;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:19:12
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ArrayMatcher extends AbstractMatcher<TypeLiteral<?>> {

    private final Class<?> arrayClass;

    public ArrayMatcher(Class<?> arrayClass) {
        this.arrayClass = arrayClass;
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        Type type = typeLiteral.getType();
        return type instanceof GenericArrayType
                &&
                ((GenericArrayType) type).getGenericComponentType().equals(arrayClass);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(ArrayMatcher.class).
                add("arrayClass", arrayClass).
                toString();
    }
}
