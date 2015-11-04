package com.maxifier.guice.events;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.06.2009
 * Time: 13:52:41
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Filter {
    Class<? extends EventMatcher> matcher() default DefaultMatcher.class;

    class DefaultMatcher implements EventMatcher {
        @Override
        public boolean matches(Object event) {
            return true;
        }
    }
}
