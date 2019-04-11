package com.maxifier.guice.property.converter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.maxifier.guice.property.Property;
import com.maxifier.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * @author Aleksey Didik (23.05.2008 10:19:35)
 */
public class URLTypeConverterTest {
    @Test
    public void testConvert() throws Exception {
        URLTypeConverter converter = new URLTypeConverter();
        String path = "http://www.maxifier.com/hello.html";
        URL url = (URL) converter.convert(path, TypeLiteral.get(URI.class));
        assertEquals(url.getHost(), "www.maxifier.com");
        assertEquals(url.getPath(), "/hello.html");
    }

    @Test
    public void testInContainer() throws ParseException, URISyntaxException, MalformedURLException {
        Properties props = new Properties();
        props.put("url", "http://maxifier.com/index.html");
        Injector inj = Guice.createInjector(PropertyModule.loadFrom(props).withConverters());
        Foo foo = inj.getInstance(Foo.class);
        URL url = foo.url;
        assertEquals(url, new URL("http://maxifier.com/index.html"));
    }

    static class Foo {
        @Inject
        @Property("url")
        URL url;
    }
}
