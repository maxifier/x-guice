package com.magenta.guice.events;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Filter(matcher = HandleAnnotated.Matcher.class)
public @interface HandleAnnotated {
    Class<? extends Annotation>[] value();

    public class Matcher implements EventClassMatcher {
        private final Class<? extends Annotation>[] value;

        @MatcherConstructor
        public Matcher(HandleAnnotated h) {
            value = h.value();
            for (Class<? extends Annotation> v : value) {
                if (!v.isAnnotationPresent(EventGroup.class)) {
                    throw new RuntimeException("Error while parsing " + h + ": annotation class " + v + " doesn't have @EventGroup annotation");
                }
            }
        }

        @Override
        public boolean matches(Class event) {
            for (Class<? extends Annotation> c : value) {
                if (event.isAnnotationPresent(c)) {
                    return true;
                }
            }
            return false;
        }
    }
}
