package com.magenta.guice.override;

import com.google.inject.Module;
import com.google.inject.util.Modules;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for OverrideModules provides collection
 * of all modules and overrides and produce final Module for
 * container creation.
 *
 * <pre>
 *     Override.collect(module1, module2);
 * </pre>
 *
 * @author aleksey.didik@maxifier.com (Aleksey Didik)
 */
public final class GuiceOverrides {

    private GuiceOverrides() {
    }

    /**
     * Collect Guice modules including any amount of {@see OverrideModule}.
     * Produce resulting modules where all override produced.
     * @param modules mix of usual and {@see OverrideModule}
     * @return resulting module for container initialization.
     */
    public static Module collect(Module... modules) {
        return collect(Arrays.asList(modules));
    }

    /**
     * Collect Guice modules including any amount of {@see OverrideModule}.
     * Produce resulting modules where all override produced.
     * @param modules mix of usual and {@see OverrideModule}
     * @return resulting module for container initialization.
     */
    public static Module collect(Iterable<? extends Module> modules) {
        List<Module> base = new LinkedList<Module>();
        List<Module> overrides = new LinkedList<Module>();
        for (Module module : modules) {
            if (module instanceof OverrideModule) {
                Module override = ((OverrideModule) module).override();
                if (override != null) {
                    overrides.add(override);
                }
            }
            base.add(module);
        }
        return Modules.override(base).with(overrides);
    }
}
