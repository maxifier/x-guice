package com.maxifier.guice.timer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.InjectionPoint;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by: Aleksey Didik
 * Date: 5/26/11
 * Time: 6:22 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class FooTest {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {


                bind(Monster.class).to(MonsterImpl.class);
                bind(Foo.class).to(FooImpl.class);

                Class<?> monitoredClass = FooImpl.class;

                Set<Class<?>> monitoredClasses = new HashSet<Class<?>>();
                monitoredClasses.add(monitoredClass);

                //get injection points of monitored class
                Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();
                injectionPoints.add(InjectionPoint.forConstructorOf(monitoredClass));
                injectionPoints.addAll(InjectionPoint.forInstanceMethodsAndFields(monitoredClass));
                injectionPoints.addAll(InjectionPoint.forStaticMethodsAndFields(monitoredClass));


                Set<Dependency<?>> dependencies = Dependency.forInjectionPoints(injectionPoints);
                for (Dependency<?> dependency : dependencies) {
                    Class<?> rawType = dependency.getKey().getTypeLiteral().getRawType();
                    monitoredClasses.add(rawType);
                    System.out.println(rawType);
                }

                //make interceptor
                ClassMatcher classMatcher = new ClassMatcher(monitoredClasses);
                FooInterceptor fooInterceptor = new FooInterceptor(monitoredClass);
                bindInterceptor(classMatcher, Matchers.any(), fooInterceptor);
            }

        });
        //get monster and operate with
        Monster monster = injector.getInstance(Monster.class);
        monster.getSomething();
        //get foo and operate with
        Foo foo = injector.getInstance(Foo.class);
        foo.saySomething();
        //operate again and catch exception
        foo.saySomething();
    }


    public static interface Monster {

        String getSomething();
    }

    public static class MonsterImpl implements Monster {

        @Override
        public String getSomething() {
            return "I'm Monster!";
        }
    }

    public static interface Foo {

        void saySomething();
    }

    public static class FooImpl implements Foo {

        private final Monster monster;
        private boolean errorFlag = false;

        @Inject
        FooImpl(Monster monster) {
            this.monster = monster;
        }

        @Override
        public void saySomething() {
            monster.getSomething();
            if (errorFlag) {
                throw new RuntimeException("Hrrrr");
            }
            errorFlag = true;
        }
    }


    public static class FooInterceptor implements MethodInterceptor {

        private final Class<?> monitoredClass;

        private boolean record = false;
        private List<String> history = new LinkedList<String>();

        public FooInterceptor(Class<?> monitoredClass) {
            this.monitoredClass = monitoredClass;
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            boolean inMonitored = Matchers.subclassesOf(monitoredClass).matches(invocation.getThis().getClass());
            if (inMonitored) {
                record = true;
            }
            try {
                Object result = invocation.proceed();
                if (record) {
                    if (inMonitored) {
                        record = false;
                        cleanHistory();
                    } else {
                        history.add("Call of " + invocation.getMethod() + ", result = " + result);
                    }
                }
                return result;
            } catch (Throwable throwable) {
                System.out.println("Error in method " + invocation.getMethod());
                printHistory();
                throw throwable;
            }
        }

        private void cleanHistory() {
            history.clear();
        }

        private void printHistory() {
            System.out.println("History of dependencies call during this method:");
            for (String s : history) {
                System.out.println(s);
            }
        }
    }

    public static class ClassMatcher extends AbstractMatcher<Class<?>> {

        private final Iterable<Class<?>> classes;

        public ClassMatcher(Class<?>... classes) {
            this(Arrays.asList(classes));
        }

        public ClassMatcher(Iterable<Class<?>> classes) {
            this.classes = classes;
        }

        @Override
        public boolean matches(Class<?> aClass) {
            for (Class<?> aClass1 : classes) {
                if (aClass.equals(aClass1) || Matchers.subclassesOf(aClass1).matches(aClass)) {
                    return true;
                }
            }
            return false;
        }
    }
}
