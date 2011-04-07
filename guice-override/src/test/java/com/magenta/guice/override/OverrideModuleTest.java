package com.magenta.guice.override;

import com.google.inject.*;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class OverrideModuleTest {
    @Test
    public void testSimpleOverride() throws Exception {
        Module base = new AbstractModule() {
            @Override
            protected void configure() {
                bind(I.class).to(OldImpl.class);
            }
        };

        Module override = new OverrideModule() {
            @Override
            protected void configure() {
                override(I.class).to(NewImpl.class);
            }
        };

        Injector inj = Guice.createInjector(OverrideModule.collect(base, override));
        I instance = inj.getInstance(I.class);
        assertTrue(instance instanceof NewImpl);
    }

    @Test
    public void testScopeOverride() throws Exception {
        Module base = new AbstractModule() {
            @Override
            protected void configure() {
                bind(I.class).to(OldImpl.class).in(Scopes.SINGLETON);
            }
        };

        Module override = new OverrideModule() {
            @Override
            protected void configure() {
                override(I.class).to(OldImpl.class).in(Scopes.NO_SCOPE);
            }
        };

        Injector inj = Guice.createInjector(OverrideModule.collect(base, override));
        I instance1 = inj.getInstance(I.class);
        I instance2 = inj.getInstance(I.class);
        assertTrue(instance1 instanceof OldImpl);
        assertTrue(instance2 instanceof OldImpl);
        assertFalse(instance1.equals(instance2));
    }

    static interface I {
    }

    static class OldImpl implements I {
    }

    static class NewImpl implements I {
    }


}
