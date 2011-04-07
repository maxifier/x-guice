package com.magenta.guice.bootstrap.activator;

/**
 * Project: Maxifier
 * Author: Aleksey Didik
 * Created: 25.02.2010
 * <p/>
 * Copyright (c) 1999-2010 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 */
public interface ActivatorManager {

    void register(Object activator);

    void activate();
}
