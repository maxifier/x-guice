package com.maxifier.guice.mbean;


public final class NoOperationsMBeanManager implements MBeanManager, com.magenta.guice.mbean.MBeanManager {
    @Override
    public void register(Object... mbeans) {
        //NOP
    }

    @Override
    public void register(Iterable<Object> mbeans) {
        //NOP
    }

    @Override
    public void register(String name, Object mbean) {
        //NOP
    }

    @Override
    public void unregister(String... name) {
        //NOP
    }

    @Override
    public void unregister(Iterable<String> names) {
        //NOP
    }

    @Override
    public void unregisterAll() {
        //NOP
    }
}
