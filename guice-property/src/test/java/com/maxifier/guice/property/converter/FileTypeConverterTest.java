package com.maxifier.guice.property.converter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.maxifier.guice.property.Property;
import com.maxifier.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Properties;

import static org.testng.Assert.assertEquals;

/**
 * @author Aleksey Didik (28.10.2009 19:28:24)
 */
public class FileTypeConverterTest {
    private static final String FILE_NAME = "foo.txt";
    private static final String PARENT_FILE_NAME = "." + File.separatorChar + "hello";
    private static final String FULL_NAME = PARENT_FILE_NAME + File.separatorChar + FILE_NAME;

    @Test
    public void testConvert() {
        FileTypeConverter converter = new FileTypeConverter();
        File file = (File) converter.convert(FULL_NAME, TypeLiteral.get(File.class));
        assertEquals(file.getName(), FILE_NAME);
        assertEquals(file.getParent(), PARENT_FILE_NAME);
    }

    @Test
    public void testInContainer() {
        Properties props = new Properties();
        props.put("file.name", FULL_NAME);
        Injector inj = Guice.createInjector(PropertyModule.loadFrom(props).withConverters());
        Foo foo = inj.getInstance(Foo.class);
        File fooFile = foo.file;
        assertEquals(fooFile.getName(), FILE_NAME);
        assertEquals(fooFile.getParent(), PARENT_FILE_NAME);
    }

    static class Foo {
        @Inject
        @Property("file.name")
        File file;
    }

}
