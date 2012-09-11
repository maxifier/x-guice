package com.maxifier.guice.mbean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.inject.TypeLiteral;
import org.junit.Test;


public class MBeanAnnotationMatcherTest {
    @Test
    public void testMatches() throws Exception {
        @SuppressWarnings({"unchecked"})
        AnnotationMatcher matcher = new AnnotationMatcher(MBean.class, com.magenta.guice.mbean.MBean.class);
        assertTrue(matcher.matches(TypeLiteral.get(MBeaned.class)));
        assertTrue(matcher.matches(TypeLiteral.get(OldMBeaned.class)));
        assertFalse(matcher.matches(TypeLiteral.get(NotMBeaned.class)));
    }

    @com.magenta.guice.mbean.MBean(name = "service=Foo")
    static class OldMBeaned {
    }


    @MBean(name = "service=Foo")
    static class MBeaned {
    }


    static class NotMBeaned {
    }
}
