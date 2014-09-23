package com.magenta.guice.property.converter;


import com.google.inject.TypeLiteral;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class ClazzMatcherTest {
    @Test
    public void testMatches() throws Exception {
        ClazzMatcher clazzMatcher = new ClazzMatcher(Integer.class);
        assertTrue(clazzMatcher.matches(TypeLiteral.get(Integer.class)));
    }
}
