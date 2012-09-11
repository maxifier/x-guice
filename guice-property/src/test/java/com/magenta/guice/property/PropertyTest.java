package com.magenta.guice.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:30:04
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class PropertyTest {

    @Test
    public void testProperty() {
        final Map<String, String> registry = new HashMap<String, String>();
        registry.put("name", "Zebra");
        registry.put("age", "4");
        registry.put("wild", "true");
        registry.put("tailLength", "15.52");
//        registry.set("planet", "Mars");

        Injector inj = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                PropertyModule.bindProperties(binder(), registry);
                bind(Animal.class).to(AnimalImpl.class);
            }
        });

        Animal animal = inj.getInstance(Animal.class);
        assertEquals("Zebra", animal.getName());
        assertEquals("Earth", animal.getPlanet());
        assertEquals("Animal age must be 4", 4, animal.getAge());
        assertTrue("Animal must be wild", animal.isWild());
        assertEquals("Animal name must be Zebra", 15.52, animal.getTailLength(), 0.0);
    }

    static class AnimalImpl implements Animal {

        private final String name;
        private final int age;

        private boolean wild;

        @Inject
        @Property("tailLength")
        private double tailLength;

        @Inject(optional = true)
        @Property("planet")
        private String planet = "Earth";

        @Inject
        public AnimalImpl(
                @Property("name") String name,
                @Property("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isWild() {
            return wild;
        }

        @Inject
        public void setWild(@Property("wild") boolean wild) {
            this.wild = wild;
        }

        public double getTailLength() {
            return tailLength;
        }

        public String getPlanet() {
            return planet;
        }

        public void setPlanet(String planet) {
            this.planet = planet;
        }
    }

    static interface Animal {

        String getName();

        int getAge();

        boolean isWild();

        double getTailLength();

        String getPlanet();

    }
}
