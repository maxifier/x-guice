package com.magenta.guice.property.converter;

import static org.junit.Assert.assertTrue;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import org.junit.Test;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:28:14
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ArrayMatcherTest {
    @Test
    public void testMatches() {
        Matcher<TypeLiteral<?>> matcher = new ArrayMatcher(String.class);
        assertTrue(matcher.matches(TypeLiteral.get(String[].class)));
    }
}
