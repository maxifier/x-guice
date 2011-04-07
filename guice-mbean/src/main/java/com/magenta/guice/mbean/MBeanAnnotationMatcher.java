package com.magenta.guice.mbean;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

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
final class MBeanAnnotationMatcher extends AbstractMatcher<TypeLiteral<?>> {
    @Override
    public boolean matches(TypeLiteral<?> typeLiteral) {
        return typeLiteral.getRawType().isAnnotationPresent(MBean.class);
    }
}
