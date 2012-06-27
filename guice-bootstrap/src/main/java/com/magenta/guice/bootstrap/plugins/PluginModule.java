package com.magenta.guice.bootstrap.plugins;

import com.google.inject.Module;
import com.magenta.guice.property.PropertiesHandler;

/**
 * Plugin modules are being loaded in child injector. Before installing such modules
 * PluginManager retrieves all properties of this module, overrides them by common properties
 * from parent container and binds into plugin injector.
 *
 * @author Igor Yankov (igor.yankov@maxifier.com)
 */
public interface PluginModule extends Module {

    /**
     * Plugin module should define all properties it needs during the work.
     * @return properties or null if it does not use own properties
     */
    PropertiesHandler getModuleProperties();
}
