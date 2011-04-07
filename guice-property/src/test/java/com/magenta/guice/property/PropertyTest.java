package com.magenta.guice.property;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
@Test
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
                bind(Animal.class).to(AnimalImpl.class);
                PropertyModule.bindProperties(binder(), registry);
            }
        });

        Animal animal = inj.getInstance(Animal.class);
        assertEquals("Zebra", animal.getName(), "Animal name must be Zebra");
        assertEquals("Earth", animal.getPlanet(), "Default value might not be overrided");
        assertEquals(4, animal.getAge(), "Animal age must be 4");
        assertTrue(animal.isWild(), "Animal must be wild");
        assertEquals(15.52, animal.getTailLength(), 0.0, "Animal name must be Zebra");
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
