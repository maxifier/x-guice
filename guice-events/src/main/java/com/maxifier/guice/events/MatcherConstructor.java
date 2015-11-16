package com.maxifier.guice.events;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.CONSTRUCTOR)
public @interface MatcherConstructor {
}
