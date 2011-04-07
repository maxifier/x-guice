package com.magenta.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class DateTypeConverter implements TypeConverter {
    private static final String SPLIT = "[#]";

    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
        String[] values = value.split(SPLIT);
        if (values.length != 2) {
            throw new IllegalArgumentException("Wrong date property format. Use {date}#{date format}");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(values[1].trim());
        try {
            return dateFormat.parse(values[0].trim());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}