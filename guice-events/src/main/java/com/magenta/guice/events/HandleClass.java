package com.magenta.guice.events;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Filter(matcher = HandleClass.Matcher.class)
public @interface HandleClass {
    Class[] value() default {};

    public class Matcher implements EventClassMatcher {
        private final Class[] classes;

        @MatcherConstructor
        public Matcher(HandleClass h) {
            classes = h.value();
        }

        @Override
        public boolean matches(@NotNull Class event) {
            if (classes.length == 0) {
                return true;
            }
            for (Class c : classes) {
                if (c.isAssignableFrom(event)) {
                    return true;
                }
            }
            return false;
        }
    }
}
