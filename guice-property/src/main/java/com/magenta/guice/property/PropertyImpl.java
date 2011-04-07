package com.magenta.guice.property;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:17:26
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class PropertyImpl implements Property, Serializable {

    private static final long serialVersionUID = 3672977046537086190L;

    private final String value;

    public PropertyImpl(String name) {
        this.value = name;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Property.class;
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Property)) {
            return false;
        }
        Property other = (Property) o;
        return value.equals(other.value());
    }

    @Override
    public String toString() {
        return String.format("@%s(value=%s)", Property.class.getSimpleName(), value);
    }
}
