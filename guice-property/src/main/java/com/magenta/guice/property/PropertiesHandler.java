package com.magenta.guice.property;

import java.util.Collection;
import java.util.Set;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:23:29
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public interface PropertiesHandler {    
    Set<String> keys();
    String get(String key);
}
