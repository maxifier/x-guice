package com.magenta.guice.decorator;

/**
 * Project: Maxifier
 * Date: 09.11.2009
 * Time: 11:44:48
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class DuplicateMemberException extends RuntimeException {

    private static final long serialVersionUID = -7298717597749779417L;

    private final Class<?> startElement;
    private final Class<?> duplicate;

    public DuplicateMemberException(Class<?> startElement, Class<?> duplicate) {
        this.startElement = startElement;
        this.duplicate = duplicate;
    }

    @SuppressWarnings({"RefusedBequest"})
    //it's ok
    @Override
    public String getMessage() {
        return String.format("Decoration chain for element %s has a duplicate member %s", startElement, duplicate);
    }
}
