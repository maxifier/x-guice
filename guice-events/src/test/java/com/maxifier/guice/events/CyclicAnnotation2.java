package com.maxifier.guice.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.06.2009
 * Time: 10:34:34
 */
@Retention(RetentionPolicy.RUNTIME)
@Filter
@CyclicAnnotation1
@interface CyclicAnnotation2 {
}
