package com.magenta.guice.decorator;

import com.google.inject.*;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.spi.Element;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.RecordingBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Author: Andrey Khayrutdinov
 * Date: 12/13/11 1:10 PM
 *
 * @author Andrey Khayrutdinov
 */
public abstract class DecoratorModule extends AbstractModule {
    private List<DecoratingAnnotatedBindingBuilder<?>> builders  = new ArrayList<DecoratingAnnotatedBindingBuilder<?>>(10);
    
    @Override
    protected <T> DecoratingAnnotatedBindingBuilder<T> bind(Class<T> clazz) {
        return createBuilder(clazz);
    }
    
    private <T> DecoratingAnnotatedBindingBuilder<T> createBuilder(Class<T> clazz) {
        DecoratingAnnotatedBindingBuilder<T> builder = new DecoratingAnnotatedBindingBuilder<T>(clazz, new RecordingBinder(Stage.TOOL));
        builders.add(builder);
        return builder;
    }

    @Override
    protected final void configure() {
        doConfigure();              

        List<DecoratingAnnotatedBindingBuilder<?>> decoratedClasses = new ArrayList<DecoratingAnnotatedBindingBuilder<?>>(builders.size());
        for (DecoratingAnnotatedBindingBuilder<?> builder : builders) {
            if (builder.hasDecorators()) {
                decoratedClasses.add(builder);
            } else {
                justRepeatBindingFrom(builder, binder());
            }
        }
        
        if (!decoratedClasses.isEmpty()) {
            handleDecorationsFor(decoratedClasses);
        }
    }      
    
    private void justRepeatBindingFrom(DecoratingAnnotatedBindingBuilder<?> builder, Binder binder) {
        for (Element element : builder.getRecordingBinder().getElements()) {
            if (element instanceof LinkedKeyBinding) {
                LinkedKeyBinding<?> binding = (LinkedKeyBinding<?>) element;
                //noinspection unchecked
                binder.bind((Key)binding.getKey()).to(binding.getLinkedKey());
            }
        }
    }
    
    private void handleDecorationsFor(List<DecoratingAnnotatedBindingBuilder<?>> builders) {
        InjectorCatcher catcher = new InjectorCatcher();
        binder().requestInjection(catcher);
        for (DecoratingAnnotatedBindingBuilder<?> builder : builders) {
            handleDecorationsFor(builder, catcher);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleDecorationsFor(DecoratingAnnotatedBindingBuilder<?> builder, InjectorCatcher catcher) {
        List<Element> elements = builder.getRecordingBinder().getElements();
        DecoratorProvider<?> first = null;
        DecoratorProvider<?> last = null;

        for (Element element : elements) {
            LinkedKeyBinding<?> binding = (LinkedKeyBinding) element;
            DecoratorProvider<?> p = new DecoratorProvider<Object>(catcher, (Key<Object>) binding.getKey());
            p.setDecoratedImpl(((Key)binding.getLinkedKey()).getTypeLiteral().getRawType());
            if (first == null) {
                first = p;
            }
            if (last != null) {
                last.setNext((DecoratorProvider)p);
            }
            last = p;
        } 
        assert (first != null);
        binder().bind((Key) first.getKey()).toProvider((Provider) first);
    }

    protected abstract void doConfigure();
}

class InjectorCatcher {
    private Injector injector;
    
    @SuppressWarnings("UnusedDeclaration")
    @Inject
    public void catchInjector(Injector injector) {
        this.injector = injector;
    }

    public Injector getInjector() {
        return injector;
    }
}

class DecoratorProvider<T> implements Provider<T> {
    private final InjectorCatcher injectorCatcher;
    private final Key<T> key;
    
    private DecoratorProvider<T> next;
    private Class<? extends T> decoratedImpl;

    DecoratorProvider(InjectorCatcher injectorCatcher, Key<T> key) {
        this.injectorCatcher = injectorCatcher;
        this.key = key;
    }

    public void setNext(DecoratorProvider<T> next) {
        this.next = next;
    }

    public void setDecoratedImpl(Class<? extends T> decoratedImpl) {
        this.decoratedImpl = decoratedImpl;
    }

    @Override
    public T get() {
        if (next == null) {
            //noinspection unchecked
            return injectorCatcher.getInjector().getInstance(decoratedImpl);
        } else {
            return injectorCatcher.getInjector().createChildInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(next.getKey()).toProvider(next);
                }
            }).getInstance(decoratedImpl);
        }
    }

    public Key<T> getKey() {
        return key;
    }
}

class DecoratingAnnotatedBindingBuilder<T> implements AnnotatedBindingBuilder<T> {
    private final Class<T> bindableClass;
    private final RecordingBinder recordingBinder;
    
    private boolean hasDecorators;
    
    private AnnotatedBindingBuilder<T> bindingBuilder;

    DecoratingAnnotatedBindingBuilder(Class<T> clazz, RecordingBinder recordingBinder) {
        this.bindableClass = clazz;
        this.recordingBinder = recordingBinder;
        this.hasDecorators = false;
    }
    
    private AnnotatedBindingBuilder<T> bind() {
        if (bindingBuilder == null) {
            bindingBuilder = recordingBinder.bind(bindableClass);
        }
        return bindingBuilder;
    }

    @Override
    public DecoratingAnnotatedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
        bind().annotatedWith(annotationType);
        return this;
    }

    @Override
    public DecoratingAnnotatedBindingBuilder<T> annotatedWith(Annotation annotation) {
        bind().annotatedWith(annotation);
        return this;
    }

    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        bind().to(implementation);
        return this;
    }

    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        bind().to(targetKey);
        return this;
    }

    public void toInstance(T instance) {
        bind().toInstance(instance);
    }

    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        bind().toProvider(providerType);
        return this;
    }

    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        bind().toProvider(providerType);
        return this;
    }

    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        bind().toProvider(providerKey);
        return this;
    }

    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        bind().toConstructor(constructor);
        return this;
    }

    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
        bind().toConstructor(constructor, type);
        return this;
    }

    @Override
    public void in(Class<? extends Annotation> scopeAnnotation) {
        bind().in(scopeAnnotation);
    }

    @Override
    public void in(Scope scope) {
        bind().in(scope);
    }

    @Override
    public void asEagerSingleton() {
        bind().asEagerSingleton();
    }
    
    // -----------------------------------------------------------------------------------------------------------------

    public DecoratingAnnotatedBindingBuilder<T> to(Class<? extends T> implementation) {
        bind().to(implementation);
        return this;
    }

    public DecoratingAnnotatedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
        bind().toProvider(provider);
        return this;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public boolean hasDecorators() {
        return hasDecorators;
    }

    public RecordingBinder getRecordingBinder() {
        return recordingBinder;
    }
    // -----------------------------------------------------------------------------------------------------------------

    public DecoratingAnnotatedBindingBuilder<T> decorate(Class<? extends T> clazz) {
        recordingBinder.bind(Key.get(bindableClass, Decorated.class)).to(clazz);
        hasDecorators = true;
        return this;
    }

    public DecoratingAnnotatedBindingBuilder<T> decorate(T instance) {
        recordingBinder.bind(Key.get(bindableClass, Decorated.class)).toInstance(instance);
        hasDecorators = true;
        return this;
    }
}