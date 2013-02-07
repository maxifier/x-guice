package com.magenta.guice.lifecycle;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.internal.BindingImpl;
import com.google.inject.internal.LinkedBindingImpl;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provide methods for stop Injector and call @PreDestroy methods for all provides scopes. <br>
 * By default you will call @PreDestroy methods only for Singletons.
 * <p/>
 * For example, if you have injector in work, and want to call @PreDestroy method for all injector Singletons,
 * just call Lifecycle.destroy(injector)
 * <p/>
 * <p/>
 * <p/>
 * Project: X-Guice
 * Date: 17.09.2009
 * Time: 14:56:19
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public final class Lifecycle {

    private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);

    private Lifecycle() {
    }

    /**
     * Destroy all toInstance bindings and all bindings in scopes, listed
     * in <i>inScopes<i>.
     * <br> Bindings in Scopes.NO_SCOPE will not be destroyed.
     *
     * @param injector injector to destroy
     * @return Errors object with destroy process errors
     */
    public static Errors destroy(Injector injector) {
        return destroy(injector, Scopes.SINGLETON);
    }

    /**
     * Destroy all toInstance bindings and all bindings in scopes, listed
     * in <i>inScopes<i>.
     * <br> Bindings in Scopes.NO_SCOPE will not be destroyed.
     *
     * @param injector injector to destroy
     * @param inScopes scopes for destroy
     * @return Errors object with destroy process errors
     */
    public static Errors destroy(Injector injector, Scope... inScopes) {
        return destroy(injector, Arrays.asList(inScopes));
    }

    /**
     * Destroy all toInstance bindings and all bindings in scopes, listed
     * in <i>inScopes<i>.
     * <br> Bindings in Scopes.NO_SCOPE will not be destroyed.
     *
     * @param injector injector to destroy
     * @param inScopes scopes for destroy
     * @return Errors object with destroy process errors
     */
    public static Errors destroy(Injector injector, Collection<Scope> inScopes) {
        Errors errors = new Errors();
        for (Binding<?> binding : injector.getBindings().values()) {
            //finish to Instance binding
            if (binding instanceof InstanceBinding) {
                destroy(((InstanceBinding) binding).getProvider().get(), errors);
            }
            if (binding instanceof ProviderInstanceBinding) {
                destroy(binding.getProvider(), errors);
            }

            //finish Provider and ProviderInstance binding
            if (binding instanceof ProviderKeyBinding<?>) {
                //noinspection unchecked
                Object providerInstance = injector.getInstance(((ProviderKeyBinding) binding).getProviderKey());
                destroy(providerInstance, errors);
            }
            //finish scopes
            Scope scope = getLinkedScope(binding);
            if (inScopes.contains(scope)) {
                destroy(binding.getProvider().get(), errors);
            }
        }
        return errors;
    }

    private static void destroy(Object destroyable, Errors errors) {
        for (Method method : destroyable.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                invokePreDestroy(errors, destroyable, method);
            }
        }

    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private static void invokePreDestroy(Errors errors, Object o, final Method method) {
        if (method.getParameterTypes().length > 0) {
            errors.addError(o,
                    new IllegalStateException(String.format("Lifecycle method '%s' must have no parameters.", method)));
        } else {
            try {
                if (!method.isAccessible()) {
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        @Override
                        public Object run() {
                            method.setAccessible(true);
                            return null;
                        }
                    });
                }
                method.invoke(o);
            } catch (Throwable e) {
                errors.addError(o, e);
            }
        }
    }

    private static Scope getLinkedScope(Binding<?> binding) {
        BindingScopingVisitor<Scope> scoper = new
                BindingScopingVisitor<Scope>() {
                    public Scope visitNoScoping() {
                        return Scopes.NO_SCOPE;
                    }

                    public Scope visitScopeAnnotation(Class<? extends Annotation>
                                                              scopeAnnotation) {
                        throw new IllegalStateException("no annotations allowed here");
                    }

                    public Scope visitScope(Scope scope) {
                        return scope;
                    }

                    public Scope visitEagerSingleton() {
                        return Scopes.SINGLETON;
                    }
                };
        do {
            Scope scope = binding.acceptScopingVisitor(scoper);
            if (scope != Scopes.NO_SCOPE) {
                return scope;
            }
            if (binding instanceof LinkedBindingImpl) {
                LinkedBindingImpl<?> linkedBinding = (LinkedBindingImpl)
                        binding;
                Injector injector = linkedBinding.getInjector();
                if (injector != null) {
                    binding =
                            injector.getBinding(linkedBinding.getLinkedKey());
                    continue;
                }
            }
            return Scopes.NO_SCOPE;
        } while (true);
    }

    public static class Errors {

        private Map<Object, Throwable> errors = new HashMap<Object, Throwable>();


        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
        private void addError(Object o, Throwable error) {
            errors.put(o, error);
        }

        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
        public void print() {
            if (errors.isEmpty()) {
                return;
            }
            StringBuilder sb = new StringBuilder("");
            sb.append("Lifecycle finish erors:\n");
            int i = 1;
            for (Map.Entry<Object, Throwable> objectThrowableEntry : errors.entrySet()) {
                sb.
                        append(i++).append(") ").
                        append("Unable to invoke @PreDestroy method of ").
                        append(objectThrowableEntry.getKey()).
                        append(", cause ").append(objectThrowableEntry.getValue());
            }
            logger.error(sb.toString());
        }

        public Map<Object, Throwable> getErrorsMap() {
            return Collections.unmodifiableMap(errors);
        }
    }
}
