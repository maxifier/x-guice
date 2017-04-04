/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * Property definition from property file.
 * <p>Holds name, value and comment placed before property.</p>
 *
 * @see PropertyReader
 * @see PropertyModule#loadProperties(java.io.InputStream)
 * @author Konstantin Lyamshin (2017-04-01 21:21)
 */
public class PropertyDefinition {
    private final String name;
    private final String value;
    private final String comment;

    public PropertyDefinition(String name, String value, String comment) {
        this.name = Preconditions.checkNotNull(name);
        this.value = Preconditions.checkNotNull(value);
        this.comment = Preconditions.checkNotNull(comment);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Nonnull
    public String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyDefinition that = (PropertyDefinition) o;
        return Objects.equal(name, that.name); // can be put in set
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return String.format("Property{%s=%s}", name, value);
    }
}
