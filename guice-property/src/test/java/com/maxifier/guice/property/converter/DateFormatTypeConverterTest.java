package com.maxifier.guice.property.converter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.maxifier.guice.property.Property;
import com.maxifier.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.Assert.assertEquals;

public class DateFormatTypeConverterTest {
    @Test
    public void testConvert() throws Exception {
        DateFormatTypeConverter converter = new DateFormatTypeConverter();
        DateFormat dateFormat = (DateFormat) converter.convert("dd/MM/yyyy", TypeLiteral.get(DateFormat.class));
        Date date = dateFormat.parse("12/11/2009");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(calendar.get(Calendar.MONTH), 10);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 12);
        assertEquals(calendar.get(Calendar.YEAR), 2009);
    }

    @Test
    public void testInContainer() throws ParseException {
        Properties props = new Properties();
        props.put("df", "dd/MM/yyyy");
        Injector inj = Guice.createInjector(PropertyModule.loadFrom(props).withConverters());
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
