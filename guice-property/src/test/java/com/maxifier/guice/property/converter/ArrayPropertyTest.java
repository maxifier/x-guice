package com.maxifier.guice.property.converter;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.maxifier.guice.property.Property;
import com.maxifier.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * Project: Maxifier
 * Date: 10.09.2009
 * Time: 15:29:42
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
@SuppressWarnings({"ALL"})
public class ArrayPropertyTest {

    @Test
    public void testArrayProperty() {
        Properties properties = new Properties();
        properties.put("names", "semen,   dima");
        properties.put("actives", "true,   false; FALSE");
        properties.put("salaries", "45.234,12312.2324;23123.433");
        properties.put("ages", "23;45");

        PropertyModule module = new PropertyModule(properties);
        Injector inj = Guice.createInjector(module, new AbstractModule() {
            @Override
            protected void configure() {
                bind(Form.class).to(FormImpl.class);
                PropertyModule.bindTypes(binder());
            }
        });

        Form form = inj.getInstance(Form.class);
        assertEquals(form.getNames(), new String[]{"semen", "dima"});
        assertEquals(form.getActives()[0], true);
        assertEquals(form.getActives()[1], false);
        assertEquals(form.getActives()[2], false);
        assertEquals(form.getSalaries()[0], 45.234);
        assertEquals(form.getSalaries()[1], 12312.2324);
        assertEquals(form.getSalaries()[2], 23123.433);
        assertEquals(form.getAges()[0], 23);
        assertEquals(form.getAges()[1], 45);
    }

    static class FormImpl implements Form {

        private String[] names;

        private int[] ages;

        private boolean[] actives;

        @Inject
        @Property("salaries")
        private double[] salaries;

        @Inject
        FormImpl(@Property("names") String[] names,
                 @Property("ages") int[] ages) {
            this.names = names;
            this.ages = ages;
        }

        @Inject
        void setActives(@Property("actives") boolean[] actives) {
            this.actives = actives;
        }

        public String[] getNames() {
            return names;
        }

        public boolean[] getActives() {
            return actives;
        }

        public double[] getSalaries() {
            return salaries;
        }

        public int[] getAges() {
            return ages;
        }
    }

    interface Form {

        String[] getNames();

        boolean[] getActives();

        double[] getSalaries();

        int[] getAges();
    }
}

