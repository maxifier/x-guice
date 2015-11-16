/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.bootstrap;

import com.google.inject.util.Modules;

import javax.inject.Provider;

/**
 * Interface of module bundle definition.
 * <p>Module bundles used for loading groups of Guice modules by theirs class names.</p>
 * <p>Module bootstrapping process:</p>
 * <ol>
 * <li>Load module class using specified or default {@code ClassLoader}.</li>
 * <li>Instantiate module using special configuration {@code Injector}.</li>
 * <li>If instance implements {@link Provider} calls {@code get()}.</li>
 * <li>If {@code get()} returns {@link Modules#EMPTY_MODULE} silently skips it.</li>
 * <li>Instance is not {@code Module} then rises IAE.</li>
 * </ol>
 * <p>Configuration injector is builded by {@link InjectorBuilder#buildConfigurationInjector()}.</p>
 *
 * @author Konstantin Lyamshin (2015-11-04 17:13)
 */
public interface ModuleBundle {
    /**
     * @return bundle name
     */
    String name();

    /**
     * @return list of module's class names
     */
    Iterable<String> modules();
}
