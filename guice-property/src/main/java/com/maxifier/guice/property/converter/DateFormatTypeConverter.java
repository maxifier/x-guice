package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

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

public class DateFormatTypeConverter implements TypeConverter {

    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
        return new SimpleDateFormat(value);
    }
}