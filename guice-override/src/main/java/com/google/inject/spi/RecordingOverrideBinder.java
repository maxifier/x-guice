/*
* Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
*/
package com.google.inject.spi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.*;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.AnnotatedElementBuilder;
import com.google.inject.internal.*;
import com.google.inject.internal.util.SourceProvider;
import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.inject.internal.InternalFlags.getIncludeStackTraceOption;

/**
 * Full copy from {@link com.google.inject.spi.Elements.RecordingBinder}
 * for the sake of {@link com.magenta.guice.override.OverrideModule}
 *
 * @author Igor Yankov (igor.yankov@maxifier.com) (2014-08-21 16:02)
 */
public class RecordingOverrideBinder implements Binder, PrivateBinder, RehashableKeys {
    private final Stage stage;
    private final Set<Module> modules;
    private final List<Element> elements;
    private final List<RehashableKeys> rehashables;
    private final Object source;
    /** The current modules stack */
    private ModuleSource moduleSource = null;
    private final SourceProvider sourceProvider;

    /** The binder where exposed bindings will be created */
    private final RecordingOverrideBinder parent;
    private final PrivateElementsImpl privateElements;

    public RecordingOverrideBinder(Stage stage) {
        this.stage = stage;
        this.modules = Sets.newHashSet();
        this.elements = Lists.newArrayList();
        this.rehashables = Lists.newArrayList();
        this.source = null;
        this.sourceProvider = SourceProvider.DEFAULT_INSTANCE.plusSkippedClasses(
                Elements.class, RecordingOverrideBinder.class, AbstractModule.class,
                ConstantBindingBuilderImpl.class, AbstractBindingBuilder.class, BindingBuilder.class);
        this.parent = null;
        this.privateElements = null;
    }

    /** Creates a recording binder that's backed by {@code prototype}. */
    private RecordingOverrideBinder(
            RecordingOverrideBinder prototype, Object source, SourceProvider sourceProvider) {
        checkArgument(source == null ^ sourceProvider == null);

        this.stage = prototype.stage;
        this.modules = prototype.modules;
        this.elements = prototype.elements;
        this.rehashables = prototype.rehashables;
        this.source = source;
        this.moduleSource = prototype.moduleSource;
        this.sourceProvider = sourceProvider;
        this.parent = prototype.parent;
        this.privateElements = prototype.privateElements;
    }

    /** Creates a private recording binder. */
    private RecordingOverrideBinder(RecordingOverrideBinder parent, PrivateElementsImpl privateElements) {
        this.stage = parent.stage;
        this.modules = Sets.newHashSet();
        this.elements = privateElements.getElementsMutable();
        this.rehashables = Lists.newArrayList();
        this.source = parent.source;
        this.moduleSource = parent.moduleSource;
        this.sourceProvider = parent.sourceProvider;
        this.parent = parent;
        this.privateElements = privateElements;
    }

    /*if[AOP]*/
    public void bindInterceptor(
            Matcher<? super Class<?>> classMatcher,
            Matcher<? super Method> methodMatcher,
            org.aopalliance.intercept.MethodInterceptor... interceptors) {
        elements.add(new InterceptorBinding(
                getElementSource(), classMatcher, methodMatcher, interceptors));
    }
    /*end[AOP]*/

    public void bindScope(Class<? extends Annotation> annotationType, Scope scope) {
        elements.add(new ScopeBinding(getElementSource(), annotationType, scope));
    }

    @SuppressWarnings("unchecked") // it is safe to use the type literal for the raw type
    public void requestInjection(Object instance) {
        requestInjection((TypeLiteral<Object>) TypeLiteral.get(instance.getClass()), instance);
    }

    public <T> void requestInjection(TypeLiteral<T> type, T instance) {
        elements.add(new InjectionRequest<T>(getElementSource(), type, instance));
    }

    public <T> MembersInjector<T> getMembersInjector(final TypeLiteral<T> typeLiteral) {
        final MembersInjectorLookup<T> element
                = new MembersInjectorLookup<T>(getElementSource(), typeLiteral);
        elements.add(element);
        return element.getMembersInjector();
    }

    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return getMembersInjector(TypeLiteral.get(type));
    }

    public void bindListener(Matcher<? super TypeLiteral<?>> typeMatcher, TypeListener listener) {
        elements.add(new TypeListenerBinding(getElementSource(), listener, typeMatcher));
    }

    public void bindListener(Matcher<? super Binding<?>> bindingMatcher,
                             ProvisionListener... listeners) {
        elements.add(new ProvisionListenerBinding(getElementSource(), bindingMatcher, listeners));
    }

    public void requestStaticInjection(Class<?>... types) {
        for (Class<?> type : types) {
            elements.add(new StaticInjectionRequest(getElementSource(), type));
        }
    }

    public void install(Module module) {
        if (modules.add(module)) {
            Binder binder = this;
            // Update the module source for the new module
            updateModuleSource(module);
            if (module instanceof PrivateModule) {
                binder = binder.newPrivateBinder();
            }
            try {
                module.configure(binder);
            } catch (RuntimeException e) {
                Collection<Message> messages = Errors.getMessagesFromThrowable(e);
                if (!messages.isEmpty()) {
                    elements.addAll(messages);
                } else {
                    addError(e);
                }
            }
            binder.install(ProviderMethodsModule.forModule(module));
            // We are done with this module, so undo module source change
            undoModuleSource(module);
        }
    }

    public void undoModuleSource(Module module) {
        if (!(module instanceof ProviderMethodsModule)) {
            moduleSource = moduleSource.getParent();
        }
    }

    public void updateModuleSource(Module module) {
        if (!(module instanceof ProviderMethodsModule)) {
            moduleSource = getModuleSource(module);
        }
    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public Stage currentStage() {
        return stage;
    }

    public void addError(String message, Object... arguments) {
        elements.add(new Message(getElementSource(), Errors.format(message, arguments)));
    }

    public void addError(Throwable t) {
        String message = "An exception was caught and reported. Message: " + t.getMessage();
        elements.add(new Message(ImmutableList.of((Object) getElementSource()), message, t));
    }

    public void addError(Message message) {
        elements.add(message);
    }

    public <T> AnnotatedBindingBuilder<T> bind(Key<T> key) {
        BindingBuilder<T> builder = new BindingBuilder<T>(this, elements, getElementSource(), key);
        rehashables.add(builder);
        return builder;
    }

    public <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return bind(Key.get(typeLiteral));
    }

    public <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return bind(Key.get(type));
    }

    public AnnotatedConstantBindingBuilder bindConstant() {
        return new ConstantBindingBuilderImpl<Void>(this, elements, getElementSource());
    }

    public <T> Provider<T> getProvider(final Key<T> key) {
        final ProviderLookup<T> element = new ProviderLookup<T>(getElementSource(), key);
        elements.add(element);
        rehashables.add(element.getKeyRehasher());
        return element.getProvider();
    }

    public <T> Provider<T> getProvider(Class<T> type) {
        return getProvider(Key.get(type));
    }

    public void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher,
                               TypeConverter converter) {
        elements.add(new TypeConverterBinding(getElementSource(), typeMatcher, converter));
    }

    public RecordingOverrideBinder withSource(final Object source) {
        return source == this.source ? this : new RecordingOverrideBinder(this, source, null);
    }

    public RecordingOverrideBinder skipSources(Class... classesToSkip) {
        // if a source is specified explicitly, we don't need to skip sources
        if (source != null) {
            return this;
        }

        SourceProvider newSourceProvider = sourceProvider.plusSkippedClasses(classesToSkip);
        return new RecordingOverrideBinder(this, null, newSourceProvider);
    }

    public PrivateBinder newPrivateBinder() {
        PrivateElementsImpl privateElements = new PrivateElementsImpl(getElementSource());
        RecordingOverrideBinder binder = new RecordingOverrideBinder(this, privateElements);
        elements.add(privateElements);
        rehashables.add(binder);
        return binder;
    }

    public void disableCircularProxies() {
        elements.add(new DisableCircularProxiesOption(getElementSource()));
    }

    public void requireExplicitBindings() {
        elements.add(new RequireExplicitBindingsOption(getElementSource()));
    }

    public void requireAtInjectOnConstructors() {
        elements.add(new RequireAtInjectOnConstructorsOption(getElementSource()));
    }

    public void requireExactBindingAnnotations() {
        elements.add(new RequireExactBindingAnnotationsOption(getElementSource()));
    }

    public void expose(Key<?> key) {
        exposeInternal(key);
    }

    public AnnotatedElementBuilder expose(Class<?> type) {
        return exposeInternal(Key.get(type));
    }

    public AnnotatedElementBuilder expose(TypeLiteral<?> type) {
        return exposeInternal(Key.get(type));
    }

    private <T> AnnotatedElementBuilder exposeInternal(Key<T> key) {
        if (privateElements == null) {
            addError("Cannot expose %s on a standard binder. "
                    + "Exposed bindings are only applicable to private binders.", key);
            return new AnnotatedElementBuilder() {
                public void annotatedWith(Class<? extends Annotation> annotationType) {}
                public void annotatedWith(Annotation annotation) {}
            };
        }

        ExposureBuilder<T> builder = new ExposureBuilder<T>(this, getElementSource(), key);
        privateElements.addExposureBuilder(builder);
        return builder;
    }

    private ModuleSource getModuleSource(Module module) {
        StackTraceElement[] partialCallStack;
        if (getIncludeStackTraceOption() == InternalFlags.IncludeStackTraceOption.COMPLETE) {
            partialCallStack = getPartialCallStack(new Throwable().getStackTrace());
        } else {
            partialCallStack = new StackTraceElement[0];
        }
        if (moduleSource == null) {
            return new ModuleSource(module, partialCallStack);
        }
        return moduleSource.createChild(module, partialCallStack);
    }

    private ElementSource getElementSource() {
        // Full call stack
        StackTraceElement[] callStack = null;
        // The call stack starts from current top module configure and ends at this method caller
        StackTraceElement[] partialCallStack = new StackTraceElement[0];
        // The element original source
        ElementSource originalSource = null;
        // The element declaring source
        Object declaringSource = source;
        if (declaringSource instanceof ElementSource) {
            originalSource = (ElementSource) declaringSource;
            declaringSource = originalSource.getDeclaringSource();
        }
        InternalFlags.IncludeStackTraceOption stackTraceOption = getIncludeStackTraceOption();
        if (stackTraceOption == InternalFlags.IncludeStackTraceOption.COMPLETE ||
                (stackTraceOption == InternalFlags.IncludeStackTraceOption.ONLY_FOR_DECLARING_SOURCE
                        && declaringSource == null)) {
            callStack = new Throwable().getStackTrace();
        }
        if (stackTraceOption == InternalFlags.IncludeStackTraceOption.COMPLETE) {
            partialCallStack = getPartialCallStack(callStack);
        }
        if (declaringSource == null) {
            // So 'source' and 'originalSource' are null otherwise declaringSource has some value
            if (stackTraceOption == InternalFlags.IncludeStackTraceOption.COMPLETE ||
                    stackTraceOption == InternalFlags.IncludeStackTraceOption.ONLY_FOR_DECLARING_SOURCE) {
                // With the above conditions and assignments 'callStack' is non-null
                declaringSource = sourceProvider.get(callStack);
            } else { // or if (stackTraceOption == IncludeStackTraceOptions.OFF)
                // As neither 'declaring source' nor 'call stack' is available use 'module source'
                declaringSource = sourceProvider.getFromClassNames(moduleSource.getModuleClassNames());
            }
        }
        // Build the binding call stack
        return new ElementSource(
                originalSource, declaringSource, moduleSource, partialCallStack);
    }

    /**
     * Removes the {@link #moduleSource} call stack from the beginning of current call stack. It
     * also removes the last two elements in order to make {@link #install(Module)} the last call
     * in the call stack.
     */
    private StackTraceElement[] getPartialCallStack(StackTraceElement[] callStack) {
        int toSkip = 0;
        if (moduleSource != null) {
            toSkip = moduleSource.getStackTraceSize();
        }
        // -1 for skipping 'getModuleSource' and 'getElementSource' calls
        int chunkSize = callStack.length - toSkip - 1;

        StackTraceElement[] partialCallStack = new StackTraceElement[chunkSize];
        System.arraycopy(callStack, 1, partialCallStack, 0, chunkSize);
        return partialCallStack;
    }

    @Override public void rehashKeys() {
        for (RehashableKeys rehashable : rehashables) {
            rehashable.rehashKeys();
        }
    }

    @Override public String toString() {
        return "Binder";
    }
}
