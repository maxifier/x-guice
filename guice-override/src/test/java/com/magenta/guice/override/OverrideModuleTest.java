package com.magenta.guice.override;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;
import com.google.inject.util.Modules;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;

import static org.testng.Assert.*;

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
    public void testOriginalOverride() throws Exception {
        Module base = new AbstractModule() {
            @Override
            protected void configure() {
                bind(I.class).to(OldImpl.class);
            }
        };

        Module override = new AbstractModule() {
            @Override
            protected void configure() {
                bind(I.class).to(NewImpl.class);
            }
        };

        Injector inj = Guice.createInjector(Modules.override(base).with(override));
        I instance = inj.getInstance(I.class);
        assertTrue(instance instanceof NewImpl);
    }


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
    public void testErrorOutput() throws Exception {
        Module base = new OverrideModule() {
            @Override
            protected void configure() {
                bind(I.class).to(OldImpl.class);
                override(I.class).to(InvalidImpl.class);
            }
        };

        Collection<Message> errorMessages = null;
        try {
            Injector inj = Guice.createInjector(OverrideModule.collect(base));
            inj.getInstance(I.class);
        } catch (CreationException e) {
            errorMessages = e.getErrorMessages();
        }
        assertNotNull(errorMessages);
        assertEquals(errorMessages.size(), 1);
        List<Object> sources = errorMessages.iterator().next().getSources();
        assertEquals(sources.size(), 3);
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

    static class InvalidImpl implements I {
        @Inject
        InvalidImpl(InvalidClass invalidClass) {
        }
    }

    static class InvalidClass {
        @Inject
        InvalidClass(@Named("a") String b) {
        }
    }


    @Test
    public void testPrivateModulesOverride() {
        Module pm1 = new PrivateModule() {
            @Override
            protected void configure() {
                bind(I.class).to(OldImpl.class);
                expose(I.class);

            }
        };
        Module pm2 = new PrivateModule() {
            @Override
            protected void configure() {
                bind(I.class).to(NewImpl.class);
                expose(I.class);
            }
        };

    }

    @Test
    public void testConstantOverride() throws Exception {
        Module m1 = new AbstractModule() {
            @Override
            protected void configure() {
                bindConstant().annotatedWith(Names.named("a")).to("Hello");
            }
        };
        Module m2 = new OverrideModule() {
            @Override
            protected void configure() {
                overrideConstant().annotatedWith(Names.named("a")).to("world");
            }
        };

        Foo instance = Guice.createInjector(OverrideModule.collect(m1, m2)).getInstance(Foo.class);
        assertEquals(instance.bu, "world");
    }

    static class Foo {
        @Inject
        @Named("a")
        String bu;
    }

    @Test
    public void testWithProvides() throws Exception {
        Module m = new AbstractModule() {
            @Override
            public void configure() {
                bind(String.class).annotatedWith(Names.named("foo")).toInstance("Hello");
            }

            @Provides
            @Named("a")
            public String provideA(@Named("foo") String foo) {
                return foo + " world";
            }
        };

        Foo instance = Guice.createInjector(OverrideModule.collect(m)).getInstance(Foo.class);
        assertEquals(instance.bu, "Hello world");
    }
}
