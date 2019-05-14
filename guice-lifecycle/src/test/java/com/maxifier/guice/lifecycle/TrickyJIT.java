package com.maxifier.guice.lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;

/**
 * @author aleksey.didik@maxifier.com (Aleksey Didik)
 */
public class TrickyJIT {

    static class Dependency {
    }

    interface Foo {
    }

    static class FooImpl implements Foo {
    }

    static class EnhancedFooImpl implements Foo {

        @Inject
        EnhancedFooImpl(Dependency dependency) {
        }
    }


    @Test
    public void testJIT() throws Exception {
        //flag to catch InjectionListener call
        final AtomicBoolean listenFlag = new AtomicBoolean();
        listenFlag.set(false);


        Injector parentInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                //nothing here
            }
        });



        Injector childInjector = parentInjector.createChildInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Dependency.class);
                bind(Foo.class).annotatedWith(Names.named("simple")).to(FooImpl.class);
                bind(Foo.class).annotatedWith(Names.named("enhanced")).to(EnhancedFooImpl.class);

                //bind listener for all injections in this injector
                bindListener(Matchers.any(), new TypeListener() {
                    @Override
                    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                        encounter.register(new InjectionListener<I>() {
                            @Override
                            public void afterInjection(I injectee) {
                                if (injectee instanceof FooImpl) {
                                    listenFlag.set(true);
                                }
                            }
                        });
                    }
                });

            }

        });

        childInjector.getInstance(Key.get(Foo.class, Names.named("simple")));

        assertEquals(listenFlag.get(), false); //the issue is still here

    }

}
