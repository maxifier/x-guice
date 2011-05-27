package com.maxifier.guice.mbean;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 9:55 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class CGLIBMBeanGeneratorTest {

    @Test
    public void testNameCompliance() throws Exception {
        CGLIBMBeanGenerator cglibmBeanGenerator = new CGLIBMBeanGenerator();
        Foo mbeanPretender = new Foo();
        Foo mbean = cglibmBeanGenerator.makeMBean(mbeanPretender);
        MBeanManagerImpl.checkCompliantion(mbean);
    }

    @Test
    public void testMethodDelegation() throws Exception {
        CGLIBMBeanGenerator cglibmBeanGenerator = new CGLIBMBeanGenerator();
        Foo mbeanPretender = new Foo();
        Foo mbean = cglibmBeanGenerator.makeMBean(mbeanPretender);
        mbean.hello();
        assertTrue(mbeanPretender.called, "mbeanPretender method have to be called");
    }

    static class Foo {

        boolean called;

        @MBeanMethod
        void hello() {
            called = true;
        }

    }
}
