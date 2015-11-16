package com.maxifier.guice.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

class HandlerAnnotation {
    private final List<EventMatcher> matchers;
    private final List<EventClassMatcher> classMatchers;
    private final Class<? extends EventMatcher> ownMatcher;

    public HandlerAnnotation(List<EventMatcher> matchers, List<EventClassMatcher> classMatchers, Class<? extends EventMatcher> ownMatcher) {
        this.matchers = matchers;
        this.classMatchers = classMatchers;
        this.ownMatcher = ownMatcher;
    }

    public void append(Annotation a, List<EventMatcher> matchers, List<EventClassMatcher> classMatchers) {
        if (this.matchers != null) {
            matchers.addAll(this.matchers);
            classMatchers.addAll(this.classMatchers);
            EventMatcher m = createMatcher(ownMatcher, a);
            if (ownMatcher != null) {
                if (EventClassMatcher.class.isAssignableFrom(ownMatcher)) {
                    classMatchers.add((EventClassMatcher) m);
                } else {
                    matchers.add(m);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends EventMatcher, A extends Annotation> T createMatcher(Class<T> cls, A a) {
        Constructor<T> found = null;
        for (Constructor<?> constructor : cls.getConstructors()) {
            MatcherConstructor mc = constructor.getAnnotation(MatcherConstructor.class);
            if (mc != null) {
                if (found != null) {
                    throw new RuntimeException("More than one constructor of " + cls + " has @MatcherConstructor");
                }
                found = (Constructor<T>) constructor;
            }
        }
        Class<?>[] pt = found.getParameterTypes();
        if (pt.length > 1) {
            throw new RuntimeException("Matcher constructor should have one or no arguments, but " + found + " requires " + pt.length);
        }
        Class<? extends Annotation> ac = a.annotationType();
        if (!pt[0].isAssignableFrom(ac)) {
            throw new RuntimeException("Matcher " + cls + " could not be bound to " + ac + " cause it doesn't have constructor that takes such annotations");
        }
        found.setAccessible(true);
        try {
            if (pt.length == 0) {
                return found.newInstance();
            } else {
                return found.newInstance(a);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
