package com.magenta.guice.mbean;

import com.google.inject.ImplementedBy;
import com.maxifier.guice.mbean.NoOperationsMBeanManager;

/**
 * Project: Maxifier
 * Date: 28.03.2008
 * Time: 8:57:43
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 * @deprecated use com.maxifier.guice.mbean.MBeanManager instead
 */
@Deprecated
@ImplementedBy(NoOperationsMBeanManager.class)
public interface MBeanManager {
    void register(Object... mbeans);

    void register(Iterable<Object> mbeans);

    void register(String name, Object mbean);

    void unregister(String... name);

    void unregister(Iterable<String> names);

    void unregisterAll();

    NoOperationsMBeanManager NO_OPERATIONS = new NoOperationsMBeanManager();

}
