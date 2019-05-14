package com.maxifier.guice.property.converter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.maxifier.guice.property.Property;
import com.maxifier.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * @author Aleksey Didik (23.05.2008 10:19:35)
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
        Properties props = new Properties();
        props.put("uri", "file://C:/test.txt");
        Injector inj = Guice.createInjector(PropertyModule.loadFrom(props).withConverters());
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
