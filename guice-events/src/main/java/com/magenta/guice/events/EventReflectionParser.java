package com.magenta.guice.events;

import gnu.trove.THashMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.06.2009
 * Time: 17:00:25
 */
class EventReflectionParser {
    private static final Map<Class<? extends Annotation>, HandlerAnnotation> annotationInfos = new THashMap();
    private static final Map<Class, ListenerClass> classInfos = new THashMap();

    /**
     * Этот объект - маркер. Перед тем, как начать обработку аннотации, в мапу annotationInfos кладется это значение.
     * После, если мы это значение достанем в ходе обработки аннотаций, это значит, что аннотация уже обрабатывается,
     * т.е. найдена циклическая зависимость.
     */
    private static final HandlerAnnotation processing = new HandlerAnnotation(null, null, null);

    private static final Lock readLock;
    private static final Lock writeLock;

    static {
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        readLock = rwl.readLock();
        writeLock = rwl.writeLock();
    }

    private static HandlerAnnotation getOrParseAnnotationInfo(Annotation a) throws CyclicFilterAnnotationException {
        Class<? extends Annotation> ac = a.annotationType();
        HandlerAnnotation c = annotationInfos.get(ac);

        // тут сравниваем именно на ссылочное равенство! т.к. объект processing является маркером циклической
        // зависимости (т.к. мы кладем именно processing, и достать должны именно его в случае циклической
        // аннотации)
        if (c == processing) {
            throw new CyclicFilterAnnotationException(ac.toString());
        }
        if (c == null) {
            c = parseAnnotationInfo(ac);
            annotationInfos.put(ac, c);
        }
        return c;
    }

    private static HandlerAnnotation parseAnnotationInfo(Class<? extends Annotation> ac) throws CyclicFilterAnnotationException {
        Filter filter = ac.getAnnotation(Filter.class);
        if (filter == null) {
            return new HandlerAnnotation(null, null, null);
        }
        annotationInfos.put(ac, processing);
        List<EventMatcher> matchers = new ArrayList<EventMatcher>();
        List<EventClassMatcher> classMatchers = new ArrayList<EventClassMatcher>();
        for (Annotation annotation : ac.getAnnotations()) {
            try {
                getOrParseAnnotationInfo(annotation).append(annotation, matchers, classMatchers);
            } catch (CyclicFilterAnnotationException e) {
                throw new CyclicFilterAnnotationException(ac.toString() + " -> " + e.getMessage());
            }
        }
        Class<? extends EventMatcher> matcher = filter.matcher();
        matcher = matcher == Filter.DefaultMatcher.class ? null : matcher;
        return new HandlerAnnotation(matchers, classMatchers, matcher);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> ListenerClass<T> getOrCreateClassInfo(Class<T> c) throws CyclicFilterAnnotationException {
        ListenerClass<T> ci;
        readLock.lock();
        try {
            ci = classInfos.get(c);
        } finally {
            readLock.unlock();
        }
        if (ci == null) {
            writeLock.lock();
            try {
                ci = classInfos.get(c);
                if (ci == null) {
                    List<HandlerMethod<T>> m = new ArrayList<HandlerMethod<T>>();
                    for (Method method : c.getDeclaredMethods()) {
                        if (isHandlerMethod(method)) {
                            m.add(EventReflectionParser.<T>parseHandlerMethod(method));
                        }
                    }

                    ci = new ListenerClass<T>(c, m);
                    classInfos.put(c, ci);
                }
            } finally {
                writeLock.unlock();
            }
        }
        return ci;
    }

    private static boolean isHandlerMethod(Method method) {
        if (!method.isAnnotationPresent(Handler.class)) {
            return false;
        }
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) {
            throw new RuntimeException("Handler method should be non-private and non-static, but " + method + " is annotated as handler");
        }
        for (Class<?> exception : method.getExceptionTypes()) {
            if (!RuntimeException.class.isAssignableFrom(exception)) {
                throw new RuntimeException("Handler method cannot throw checked exceptions, but " + method + " does (" + exception + ")");
            }
        }
        return true;
    }

    private static <T> HandlerMethod<T> parseHandlerMethod(Method method) throws CyclicFilterAnnotationException {
        List<EventMatcher> matchers = new ArrayList<EventMatcher>();
        List<EventClassMatcher> classMatchers = new ArrayList<EventClassMatcher>();
        for (Annotation a : method.getAnnotations()) {
            getOrParseAnnotationInfo(a).append(a, matchers, classMatchers);
        }
        return new HandlerMethod<T>(matchers, classMatchers, new ReflectionHandlerInvocator<Object, T>(method));
//        return new HandlerMethod<T>(matchers, classMatchers, new ClassgenHandlerInvocator<Object, T>(method));
    }
}
