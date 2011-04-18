package com.magenta.guice.property.converter;

import com.google.inject.*;
import com.magenta.guice.property.Property;
import com.magenta.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class DateTypeConverterTest {
    @Test
    public void testConvert() throws Exception {
        DateTypeConverter dateTypeConverter = new DateTypeConverter();
        Date date = (Date) dateTypeConverter.convert("12/11/2009 # dd/MM/yyyy", TypeLiteral.get(Date.class));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(10, calendar.get(Calendar.MONTH));
        assertEquals(12, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(2009, calendar.get(Calendar.YEAR));
    }

    @Test
    public void testWrongValue() throws Exception {
        try {
            DateTypeConverter dateTypeConverter = new DateTypeConverter();
            dateTypeConverter.convert("12/11/2009", TypeLiteral.get(Date.class));
            fail("Must be unable to parse wrong format");
        } catch (IllegalArgumentException e) {
            //it's ok!
        }
    }

    @Test
    public void testInContainer() throws ParseException {
        Map<String, String> props = new HashMap<String, String>();
        props.put("date", "12/11/2009 # dd/MM/yyyy");
        Injector inj = Guice.createInjector(new PropertyModule(props), new Module() {
            @Override
            public void configure(Binder binder) {
                PropertyModule.bindTypes(binder);
            }
        });
        Foo foo = inj.getInstance(Foo.class);
        Date date = foo.date;
        assertEquals(date, new SimpleDateFormat("dd/MM/yyyy").parse("12/11/2009"));
    }

    static class Foo {
        @Inject
        @Property("date")
        Date date;
    }
}

