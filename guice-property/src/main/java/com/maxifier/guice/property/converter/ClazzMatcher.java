package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

/**
 * @author Aleksey Didik (28.10.2009 19:20:33)
 */
public class ClazzMatcher extends AbstractMatcher<TypeLiteral<?>> {

    private final Class<?> clazz;

    public ClazzMatcher(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        return clazz.isAssignableFrom(typeLiteral.getRawType());
    }
}
