package com.maxifier.guice.property.converter;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.maxifier.guice.property.Property;
import com.maxifier.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/*
* @author Aleksey Didik (23.05.2008 10:19:35)
*/
public class DateTypeConverterTest {
    @Test
    public void testConvert() throws Exception {
        DateTypeConverter dateTypeConverter = new DateTypeConverter();
        Date date = (Date) dateTypeConverter.convert("12/11/2009 # dd/MM/yyyy", TypeLiteral.get(Date.class));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(calendar.get(Calendar.MONTH), 10);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 12);
        assertEquals(calendar.get(Calendar.YEAR), 2009);
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
        Properties props = new Properties();
        props.put("date", "12/11/2009 # dd/MM/yyyy");
        Injector inj = Guice.createInjector(PropertyModule.loadFrom(props).withConverters());
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

