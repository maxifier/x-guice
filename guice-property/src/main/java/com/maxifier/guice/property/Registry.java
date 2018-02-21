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
    String getOrDefault(String key, String defaultValue);

    /**
     * @return property metadata or null if not found
     */
    @Nullable
    PropertyDefinition getDefinition(String key);

    /**
     * Update property value
     */
    void set(String key, String value);

    /**
     * Persist changes in persistent storage
     */
    void store();
}
