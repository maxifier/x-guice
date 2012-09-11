package com.magenta.guice.property.converter;


import static org.junit.Assert.assertTrue;

import com.google.inject.TypeLiteral;
import org.junit.Test;

public class ClazzMatcherTest {
    @Test
    public void testMatches() throws Exception {
        ClazzMatcher clazzMatcher = new ClazzMatcher(Integer.class);
        assertTrue(clazzMatcher.matches(TypeLiteral.get(Integer.class)));
    }
}
