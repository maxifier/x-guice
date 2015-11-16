package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

/**
 * Project: Maxifier
 * Date: 08.11.2009
 * Time: 15:28:57
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class ArrayTypeConverter implements TypeConverter {

    private final String splitRegExp;
    private final Parser parser;

    public ArrayTypeConverter(String regex, Parser parser) {
        this.splitRegExp = regex;
        this.parser = parser;
    }

    public ArrayTypeConverter(Parser parser) {
        this("[,|;]", parser);
    }

    @SuppressWarnings({"ZeroLengthArrayAllocation"})
    //it's ok framework usage
    @Override
    public Object convert(String s, TypeLiteral<?> typeLiteral) {
        String[] strings = s.split(splitRegExp);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i].trim();
        }
        if (strings.length == 1 && "".equals(strings[0])) {
            return parser.parse(new String[0]);
        }
        return parser.parse(strings);
    }

    @Override
    public String toString() {
        return "StringArrayTypeConverter{" +
                "splitRegExp='" + splitRegExp + '\'' +
                '}';
    }


    interface Parser {
        Object parse(String[] strings);
    }

    public static final ArrayTypeConverter STRING_ARRAY_CONVERTER =
            new ArrayTypeConverter(new ArrayTypeConverter.Parser() {
                @Override
                public String[] parse(String[] strings) {
                    return strings;
                }
            });

    public static final ArrayTypeConverter INT_ARRAY_CONVERTER =
            new ArrayTypeConverter(new ArrayTypeConverter.Parser() {
                @Override
                public int[] parse(String[] strings) {
                    int[] ints = new int[strings.length];
                    for (int i = 0; i < strings.length; i++) {
                        ints[i] = Integer.valueOf(strings[i]);
                    }
                    return ints;
                }
            });

    public static final ArrayTypeConverter BOOLEAN_ARRAY_CONVERTER =
            new ArrayTypeConverter(new ArrayTypeConverter.Parser() {
                @Override
                public Object parse(String[] strings) {
                    boolean[] bools = new boolean[strings.length];
                    for (int i = 0; i < strings.length; i++) {
                        bools[i] = Boolean.valueOf(strings[i]);
                    }
                    return bools;
                }
            });

    public static final ArrayTypeConverter DOUBLE_ARRAY_CONVERTER =
            new ArrayTypeConverter(new ArrayTypeConverter.Parser() {
                @Override
                public double[] parse(String[] strings) {
                    double[] doubles = new double[strings.length];
                    for (int i = 0; i < strings.length; i++) {
                        doubles[i] = Double.valueOf(strings[i]);
                    }
                    return doubles;
                }
            });
}

