package com.maxifier.guice.mbean;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 10:08 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class MBeanGenerationException extends Exception {

    public MBeanGenerationException() {
    }

    public MBeanGenerationException(String s) {
        super(s);
    }

    public MBeanGenerationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MBeanGenerationException(Throwable throwable) {
        super(throwable);
    }
}
