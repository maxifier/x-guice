package com.magenta.guice.property.converter;

import com.google.inject.TypeLiteral;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class ClazzMatcherTest {
    @Test
    public void testMatches() throws Exception {
        ClazzMatcher clazzMatcher = new ClazzMatcher(Integer.class);
        assertTrue(clazzMatcher.matches(TypeLiteral.get(Integer.class)));
    }
}
