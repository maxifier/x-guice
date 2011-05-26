package com.maxifier.guice.mbean;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 9:47 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public interface MBeanGenerator {

    <T> T makeMBean(T mbeanPretender) throws MBeanGenerationException;

}
