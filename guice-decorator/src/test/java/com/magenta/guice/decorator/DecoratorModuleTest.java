package com.magenta.guice.decorator;

import static com.google.inject.Guice.createInjector;
import static junit.framework.Assert.assertEquals;

import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Author: Andrey Khayrutdinov
 * Date: 12/13/11 5:02 PM
 *
 * @author Andrey Khayrutdinov
 */

public class DecoratorModuleTest {

    @Test
    public void testChainOfThree() {
        DecoratorModule module = new DecoratorModule() {
            @Override
            protected void doConfigure() {
                bind(DecoratableInterface.class)
                        .to(Nil.class)
                        .decorate(Two.class)
                        .decorate(Three.class);
            }
        };
        DecoratableInterface decoratable = createInjector(module).getInstance(DecoratableInterface.class);
        assertEquals(decoratable.compute(), new Two(new Three()).compute());
    }

    static interface DecoratableInterface {
        int compute();
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
    @BindingAnnotation
    static @interface A4Test {

    }

    static class Nil implements DecoratableInterface {
        private final DecoratableInterface next;

        @Inject
        Nil(@Decorated DecoratableInterface next) {
            this.next = next;
        }

        @Override
        public int compute() {
            return (next == null) ? 0 : next.compute();
        }
    }

    static class Two implements DecoratableInterface {
        private final DecoratableInterface next;

        @Inject
        Two(@Decorated DecoratableInterface next) {
            this.next = next;
        }

        @Override
        public int compute() {
            return 2 + ((next == null) ? 0 : next.compute());
        }
    }

    static class Three implements DecoratableInterface {
        @Inject
        Three() {
        }

        @Override
        public int compute() {
            return 3;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Test
    public void testThatNoDecorationCausesNothing() {
        DecoratorModule module = new DecoratorModule() {
            @Override
            protected void doConfigure() {
                bind(DecoratableInterface.class).to(Three.class);
                bind(Collection.class).to(ArrayList.class);
            }
        };

        DecoratableInterface decoratable = createInjector(module).getInstance(DecoratableInterface.class);
        assertEquals(decoratable.compute(), new Three().compute());
    }

    @Test()
    public void testDecorationWithAnnotation() {
        DecoratorModule module = new DecoratorModule() {
            @Override
            protected void doConfigure() {
                bind(DecoratableInterface.class).to(Nil.class).annotatedWith(A4Test.class).decorate(Two.class).decorate(Three.class);
            }
        };
        assertEquals(createInjector(module).getInstance(Key.get(DecoratableInterface.class, A4Test.class)).compute(), new Two(new Three()).compute());
    }
}
