package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import org.testng.annotations.Test;

import static com.maxifier.guice.property.converter.ArrayTypeConverter.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Aleksey Didik (10.09.2009 15:28:54)
 */
public class ArrayTypeConverterTest {
    @Test
    public void testConvert() {
        String[] result = (String[]) STRING_ARRAY_CONVERTER.convert("first, second; last", TypeLiteral.get(String[].class));
        assertEquals(result[0], "first");
        assertEquals(result[1], "second");
        assertEquals(result[2], "last");
    }

    @Test
    public void testConvertEmptyString() {
        String[] result = (String[]) STRING_ARRAY_CONVERTER.convert("", TypeLiteral.get(String[].class));
        assertTrue(result.length == 0);
    }

    @Test
    public void testInt() {
        int[] result = (int[]) INT_ARRAY_CONVERTER.convert("1;3,  4", TypeLiteral.get(Integer[].class));
        assertEquals(result[0], 1);
        assertEquals(result[1], 3);
        assertEquals(result[2], 4);
    }

    @Test
    public void testBoolean() {
        boolean[] result = (boolean[]) BOOLEAN_ARRAY_CONVERTER.convert("false,TRUE,FALSE,true,TruE,False", TypeLiteral.get(Integer[].class));
        assertEquals(result[0], false);
        assertEquals(result[1], true);
        assertEquals(result[2], false);
        assertEquals(result[3], true);
        assertEquals(result[4], true);
        assertEquals(result[5], false);
    }

    @Test
    public void testDouble() {
        double[] result = (double[]) DOUBLE_ARRAY_CONVERTER.convert("1.33;3.7,  4", TypeLiteral.get(Double[].class));
        assertEquals(result[0], 1.33, 0.0001);
        assertEquals(result[1], 3.7, 0.0001);
        assertEquals(result[2], 4.0, 0.0001);
    }
}
