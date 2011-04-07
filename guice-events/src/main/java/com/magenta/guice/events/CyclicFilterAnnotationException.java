package com.magenta.guice.events;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.06.2009
 * Time: 14:04:06
 */
public final class CyclicFilterAnnotationException extends RuntimeException {
    public CyclicFilterAnnotationException(String message) {
        super(message);
    }
}
