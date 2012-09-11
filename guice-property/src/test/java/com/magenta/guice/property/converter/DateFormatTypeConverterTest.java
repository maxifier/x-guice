package com.magenta.guice.property.converter;

import static org.junit.Assert.assertEquals;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.magenta.guice.property.Property;
import com.magenta.guice.property.PropertyModule;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateFormatTypeConverterTest {
    @Test
    public void testConvert() throws Exception {
        DateFormatTypeConverter converter = new DateFormatTypeConverter();
        DateFormat dateFormat = (DateFormat) converter.convert("dd/MM/yyyy", TypeLiteral.get(DateFormat.class));
        Date date = dateFormat.parse("12/11/2009");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(10, calendar.get(Calendar.MONTH));
        assertEquals(12, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(2009, calendar.get(Calendar.YEAR));
    }

    @Test
    public void testInContainer() throws ParseException {
        Map<String, String> props = new HashMap<String, String>();
        props.put("df", "dd/MM/yyyy");
        Injector inj = Guice.createInjector(new PropertyModule(props), new AbstractModule() {
            @Override
            protected void configure() {
                PropertyModule.bindTypes(binder());
            }
        });
        Foo foo = inj.getInstance(Foo.class);
        DateFormat df = foo.df;
        assertEquals(df.parse("12/11/2009"), new SimpleDateFormat("dd/MM/yyyy").parse("12/11/2009"));
    }

    static class Foo {
        @Inject
        @Property("df")
        DateFormat df;
    }
}
