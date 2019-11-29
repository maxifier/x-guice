/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * Application property registry.
 * <p>Implementors keep all application properties, handles overrides and persistence.</p>
 *
 * @see CascadeRegistry
 * @author Konstantin Lyamshin (2015-12-17 12:36)
 */
@ParametersAreNonnullByDefault
public interface Registry {
    /**
     * @return list registered properties
     */
    @Nonnull
    Set<String> keys();

    /**
     * @return property value or null if no such property found
     */
    @Nullable
    String get(String key);

    /**
     * @return property value or {@code defaultValue} if no such property found
     */
    @Nonnull
    default String getOrDefault(String key, String defaultValue)  {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Update property value
     */
    void set(String key, String value);

    /**
     * Persist changes in persistent storage
     */
    void store();

    default int getInt(String key)  {
        return Integer.parseInt(get(key));
    }

    default int getIntOrDefault(String key, int defaultValue) {
        String s = get(key);
        return s != null ? Integer.parseInt(s) : defaultValue;
    }

    default long getLong(String key)  {
        return Long.parseLong(get(key));
    }

    default long getLongOrDefault(String key, long defaultValue) {
        String s = get(key);
        return s != null ? Long.parseLong(s) : defaultValue;
    }

    default double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    default double getDoubleOrDefault(String key, double defaultValue) {
        String s = get(key);
        return s != null ? Double.parseDouble(s) : defaultValue;
    }

    default boolean getBoolean(String key) {
        return getBooleanOrDefault(key, false);
    }

    default boolean getBooleanOrDefault(String key, boolean defaultValue) {
        final String v = get(key);
        if (v == null) {
            return defaultValue;
        }
        return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes");
    }

}
