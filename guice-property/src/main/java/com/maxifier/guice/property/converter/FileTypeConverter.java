package com.maxifier.guice.property.converter;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeConverter;

import java.io.File;

/**
 * Project: Maxifier
 * Date: 28.10.2009
 * Time: 19:23:18
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class FileTypeConverter implements TypeConverter {

    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
        return new File(value);
    }
}
