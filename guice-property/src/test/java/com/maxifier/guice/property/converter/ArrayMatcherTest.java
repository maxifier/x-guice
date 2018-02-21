package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * @author Aleksey Didik (10.09.2009 15:28:14)
 */
public class ArrayMatcherTest {
    @Test
    public void testMatches() {
        Matcher<TypeLiteral<?>> matcher = new ArrayMatcher(String.class);
        assertTrue(matcher.matches(TypeLiteral.get(String[].class)));
    }
}
