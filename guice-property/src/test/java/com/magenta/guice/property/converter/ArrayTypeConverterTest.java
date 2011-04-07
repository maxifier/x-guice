package com.magenta.guice.property.converter;

import com.google.inject.TypeLiteral;
import org.testng.annotations.Test;

import static com.magenta.guice.property.converter.ArrayTypeConverter.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:28:54
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ArrayTypeConverterTest {
    @Test
    public void testConvert() {
        ArrayTypeConverter converter = STRING_ARRAY_CONVERTER;
        String[] result = (String[]) converter.convert("first, second; last", TypeLiteral.get(String[].class));
        assertEquals(result[0], "first");
        assertEquals(result[1], "second");
        assertEquals(result[2], "last");
    }

    @Test
    public void testConvertEmptyString() {
        ArrayTypeConverter converter = STRING_ARRAY_CONVERTER;
        String[] result = (String[]) converter.convert("", TypeLiteral.get(String[].class));
        assertTrue(result.length == 0);
    }

    @Test
    public void testInt() {
        ArrayTypeConverter converter = INT_ARRAY_CONVERTER;
        int[] result = (int[]) converter.convert("1;3,  4", TypeLiteral.get(Integer[].class));
        assertEquals(result[0], 1);
        assertEquals(result[1], 3);
        assertEquals(result[2], 4);
    }

    @Test
    public void testBoolean() {
        ArrayTypeConverter converter = BOOLEAN_ARRAY_CONVERTER;
        boolean[] result = (boolean[]) converter.convert("false,TRUE,FALSE,true,TruE,False", TypeLiteral.get(Integer[].class));
        assertEquals(result[0], false);
        assertEquals(result[1], true);
        assertEquals(result[2], false);
        assertEquals(result[3], true);
        assertEquals(result[4], true);
        assertEquals(result[5], false);
    }

    @SuppressWarnings({"MagicNumber"})
    @Test
    public void testDouble() {
        ArrayTypeConverter converter = DOUBLE_ARRAY_CONVERTER;
        double[] result = (double[]) converter.convert("1.33;3.7,  4", TypeLiteral.get(Double[].class));
        assertEquals(result[0], 1.33);
        assertEquals(result[1], 3.7);
        assertEquals(result[2], 4.0);
    }
}