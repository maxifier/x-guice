package com.maxifier.guice.mbean;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

import java.lang.annotation.Annotation;

/**
 * Project: Maxifier
 * Date: 17.08.2009
 * Time: 13:31:25
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public final class AnnotationMatcher extends AbstractMatcher<TypeLiteral<?>> {

    private final Class<? extends Annotation>[] annotations;

    public AnnotationMatcher(Class<? extends Annotation>... annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        for (Class<? extends Annotation> annotation : annotations) {
            if (typeLiteral.getRawType().isAnnotationPresent(annotation)) {
                return true;
            }
        }
        return false;
    }
}
