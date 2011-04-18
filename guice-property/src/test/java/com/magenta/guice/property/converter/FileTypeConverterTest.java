package com.magenta.guice.property.converter;

import com.google.inject.*;
import com.magenta.guice.property.Property;
import com.magenta.guice.property.PropertyModule;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Project: Maxifier
 * Date: 28.10.2009
 * Time: 19:28:24
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
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
        Map<String, String> props = new HashMap<String, String>();
        props.put("file.name", FULL_NAME);
        Injector inj = Guice.createInjector(new PropertyModule(props), new Module() {
            @Override
            public void configure(Binder binder) {
                PropertyModule.bindTypes(binder);
            }
        });
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
