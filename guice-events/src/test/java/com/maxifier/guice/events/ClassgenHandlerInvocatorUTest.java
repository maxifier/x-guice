package com.maxifier.guice.events;

import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;


/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.06.2009
 * Time: 8:48:58
 */
public class ClassgenHandlerInvocatorUTest {
    public interface TestInvoker {
        public void doIt(String s);

        public void doIt();

        public String returnIt();

        public String returnIt(String s);
    }

    @Test
    public void testWParam() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("doIt", String.class));

        TestInvoker test = mock(TestInvoker.class);

        assertEquals(inv.getParamType(), String.class);

        assertNull(inv.invoke(test, "test"));

        verify(test).doIt("test");
        verifyNoMoreInteractions(test);
    }

    @Test
    public void testWOParam() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("doIt"));

        TestInvoker test = mock(TestInvoker.class);

        assertEquals(inv.getParamType(), null);

        assertNull(inv.invoke(test, "test"));

        verify(test).doIt();
        verifyNoMoreInteractions(test);
    }

    @Test
    public void testWReturn() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("returnIt"));

        TestInvoker test = mock(TestInvoker.class);
        when(test.returnIt()).thenReturn("result");

        assertEquals(inv.getParamType(), null);

        assertEquals(inv.invoke(test, "test"), "result");

        verify(test).returnIt();
        verifyNoMoreInteractions(test);
    }

    @Test
    public void testWReturnAndParam() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("returnIt", String.class));

        TestInvoker test = mock(TestInvoker.class);
        when(test.returnIt("test")).thenReturn("result");

        assertEquals(inv.getParamType(), String.class);

        assertEquals(inv.invoke(test, "test"), "result");

        verify(test).returnIt("test");
        verifyNoMoreInteractions(test);
    }
}
