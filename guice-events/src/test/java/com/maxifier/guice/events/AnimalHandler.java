package com.maxifier.guice.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Filter(matcher = AnimalHandler.Matcher.class)
@HandleClass(Animal.class)
public @interface AnimalHandler {
    Animal[] value();

    class Matcher extends EnumMatcher<Animal> {
        @MatcherConstructor
        public Matcher(AnimalHandler handler) {
            super(handler.value());
        }
    }
}
