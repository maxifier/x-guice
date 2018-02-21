package com.maxifier.guice.property;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binding annotation which marks individual property values in an injector.
 * <p>To get instance of &amp;Property call {@link PropertyModule#property(String)}.</p>
 *
 * @see PropertyModule
 * @author Aleksey Didik (10.09.2009 15:17:12)
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Property {
    String value();
}
