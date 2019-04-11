package com.maxifier.guice.events;

import org.mockito.Mockito;
import org.testng.annotations.Test;

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

        TestInvoker test = Mockito.mock(TestInvoker.class);

        assertEquals(inv.getParamType(), String.class);

        assertNull(inv.invoke(test, "test"));

        Mockito.verify(test).doIt("test");
        Mockito.verifyNoMoreInteractions(test);
    }

    @Test
    public void testWOParam() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("doIt"));

        TestInvoker test = Mockito.mock(TestInvoker.class);

        assertEquals(inv.getParamType(), null);

        assertNull(inv.invoke(test, "test"));

        Mockito.verify(test).doIt();
        Mockito.verifyNoMoreInteractions(test);
    }

    @Test
    public void testWReturn() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("returnIt"));

        TestInvoker test = Mockito.mock(TestInvoker.class);
        Mockito.when(test.returnIt()).thenReturn("result");

        assertEquals(inv.getParamType(), null);

        assertEquals(inv.invoke(test, "test"), "result");

        Mockito.verify(test).returnIt();
        Mockito.verifyNoMoreInteractions(test);
    }

    @Test
    public void testWReturnAndParam() throws Exception {
        HandlerInvocator<String, TestInvoker> inv = new ClassgenHandlerInvocator<String, TestInvoker>(TestInvoker.class.getMethod("returnIt", String.class));

        TestInvoker test = Mockito.mock(TestInvoker.class);
        Mockito.when(test.returnIt("test")).thenReturn("result");

        assertEquals(inv.getParamType(), String.class);

        assertEquals(inv.invoke(test, "test"), "result");

        Mockito.verify(test).returnIt("test");
        Mockito.verifyNoMoreInteractions(test);
    }
}
