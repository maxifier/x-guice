package com.magenta.guice.override;

import com.google.inject.Module;

import javax.annotation.Nullable;


/**
 * Base interface for all modules should contain declared overrides.
 * To use override you should implement {@link OverrideModule#override()}
 * and return instance of Guice {@link com.google.inject.Module}.
 * The instance have to contain binding what will be threaten as overrides for
 * bindings in usual bindings.
 * <p>
 * <pre>
 *     <code>
 *         Module moduleWithOverride = new OverrideModule() {
 *             public void configure(Binder binder) {
 *                 binder.bind(Foo.class).to(FooImpl.class);
 *             }
 *
 *             public Module override() {
 *                 return new AbstractModule() {
 *                     bind(Foo.class).to(FooImplOverride.class);
 *                 }
 *             }
 *         };
 *         ...
 *
 *         Override.collect(moduleWithOverride, ...)
 *     </code>
 * </pre>
 *
 * {@link OverrideModule#override()} could return null if no overrides declared.
 */
public interface OverrideModule extends Module {

    @Nullable
    Module override();

}
