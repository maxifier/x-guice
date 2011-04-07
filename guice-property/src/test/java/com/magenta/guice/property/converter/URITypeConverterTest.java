package com.magenta.guice.property.converter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.magenta.guice.property.Property;
import com.magenta.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class URITypeConverterTest {
    @Test
    public void testConvert() throws Exception {
        URITypeConverter converter = new URITypeConverter();
        String path = "file://C:/test.txt";
        URI uri = (URI) converter.convert(path, TypeLiteral.get(URI.class));
        assertEquals(uri.getScheme(), "file");
        assertEquals(uri.getHost(), "C");
        assertEquals(uri.getPath(), "/test.txt");
    }

    @Test
    public void testInContainer() throws ParseException, URISyntaxException {
        Map<String, String> props = new HashMap<String, String>();
        props.put("uri", "file://C:/test.txt");
        Injector inj = Guice.createInjector(new PropertyModule(props));
        Foo foo = inj.getInstance(Foo.class);
        URI uri = foo.uri;
        assertEquals(uri, new URI("file://C:/test.txt"));
    }

    static class Foo {
        @Inject
        @Property("uri")
        URI uri;
    }
}
