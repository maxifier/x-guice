package com.maxifier.guice.mbean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeEncounter;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Test;

import java.lang.reflect.Method;

/*
* Project: Smart Advertising
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class MBeanTypeListenerTest {

    @Test
    public void testHear() throws Exception {
        MBeanTypeListener listener = new MBeanTypeListener();
        TypeLiteral<String> o1 = TypeLiteral.get(String.class);
        TypeLiteral<Integer> o2 = TypeLiteral.get(Integer.class);
        TypeLiteral<Double> o3 = TypeLiteral.get(Double.class);
        TypeLiteral<Float> o4 = TypeLiteral.get(Float.class);
        listener.hear(o1, new MockEncounter(o1));
        listener.hear(o2, new MockEncounter(o2));
        listener.hear(o3, new MockEncounter(o3));
        MBeanManager mockManager = mock(MBeanManager.class);
        listener.setManager(mockManager);
        listener.hear(o4, new MockEncounter(o4));
        verify(mockManager).register(o1);
        verify(mockManager).register(o2);
        verify(mockManager).register(o3);
        verify(mockManager).register(o4);

    }

    class MockEncounter<T> implements TypeEncounter<T> {

        private Object o;

        MockEncounter(T o) {
            this.o = o;
        }

        @Override
        public void register(InjectionListener injectionListener) {
            injectionListener.afterInjection(o);
        }

        @Override
        public void addError(String message, Object... arguments) {
        }

        @Override
        public void addError(Throwable t) {
        }

        @Override
        public void addError(Message message) {
        }

        @Override
        public void bindInterceptor(Matcher<? super Method> methodMatcher, MethodInterceptor... interceptors) {
        }

        @Override
        public void register(MembersInjector membersInjector) {
        }

        @Override
        public MembersInjector getMembersInjector(Class type) {
            return null;
        }

        @Override
        public MembersInjector getMembersInjector(TypeLiteral typeLiteral) {
            return null;
        }

        @Override
        public Provider getProvider(Class type) {
            return null;
        }

        @Override
        public Provider getProvider(Key key) {
            return null;
        }
    }
}
