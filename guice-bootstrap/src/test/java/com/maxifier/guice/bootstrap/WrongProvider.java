/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import javax.inject.Provider;

/**
 * @author Konstantin Lyamshin (2015-11-05 20:18)
 */
public class WrongProvider implements Provider<String> {
    @Override
    public String get() {
        return "WrongProvider";
    }
}
