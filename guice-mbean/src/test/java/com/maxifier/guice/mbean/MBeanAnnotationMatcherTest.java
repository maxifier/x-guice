package com.maxifier.guice.mbean;

import com.google.inject.TypeLiteral;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class MBeanAnnotationMatcherTest {
    @Test
    public void testMatches() throws Exception {
        @SuppressWarnings({"unchecked"})
        AnnotationMatcher matcher = new AnnotationMatcher(MBean.class);
        assertTrue(matcher.matches(TypeLiteral.get(MBeaned.class)));
        assertFalse(matcher.matches(TypeLiteral.get(NotMBeaned.class)));
    }

    @MBean(name = "service=Foo")
    static class MBeaned {
    }


    static class NotMBeaned {
    }
}
