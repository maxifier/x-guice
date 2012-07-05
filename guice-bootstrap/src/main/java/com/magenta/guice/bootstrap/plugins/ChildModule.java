package com.magenta.guice.bootstrap.plugins;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.magenta.guice.property.PropertiesHandler;

/**
 * This module may be loaded in child injector. Before installing this module
 * PluginManager invokes {@link ChildModule#beforeChildInjectorCreating(com.google.inject.Injector)}  passing injector of parent container as parameter.
 * <p/>So implementation can analyse bindings in the parent container and bind only new necessary services.
 *
 * @author Igor Yankov (igor.yankov@maxifier.com)
 */
public interface ChildModule extends Module {

    /**
     * Method is invoked before creation of the child injector
     * @param injector of the parent container
     */
    void beforeChildInjectorCreating(Injector injector);
}
