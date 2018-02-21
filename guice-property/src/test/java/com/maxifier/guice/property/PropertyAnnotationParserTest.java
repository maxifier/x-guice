/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.Map;

import static com.maxifier.guice.property.PropertyModule.getPropertyAnnotation;

/**
 * @author Konstantin Lyamshin (2017-04-03 22:14)
 */
public class PropertyAnnotationParserTest extends org.testng.Assert {

    @DataProvider
    Object[][] provideAnnotations() {
        return new String[][] {
            { "", "" },
            { "line1", "=line1" },
            { "\tline1\f\n", "=line1" },
            { "@attr", "@attr="},
            { "line1\n\nline2\n", "=line1\n\nline2" },
            { "line1\n\n@attr val\n", "=line1,@attr=val" },
            { "line1\n\n@attr\n@bttr", "=line1,@attr=,@bttr=" },
            { "line1\n\n@attr val\n@bttr vbl", "=line1,@attr=val,@bttr=vbl" },
            { " @attr1 val1\n\t@attr2\tval2\r\t val3\t\n", "@attr1=val1,@attr2=val2\r\t val3" },
        };
    }

    @Test(dataProvider = "provideAnnotations")
    public void testParseAnnotations(String test, String extected) throws Exception {
        Iterator<Map.Entry<String, String>> parser = new PropertyAnnotationParser(test);
        for (String line : Splitter.on(',').omitEmptyStrings().split(extected)) {
            assertEquals(parser.hasNext(), true);
            assertEquals(parser.next().toString(), line);
        }
        assertEquals(parser.hasNext(), false);
    }

    @Test
    public void testGetPropertyAnnotation() throws Exception {
        String[] text = {
            " comment",
            " @multiline true",
            " check",
            " @type text"
        };

        PropertyDefinition definition = new PropertyDefinition("k", "v", Joiner.on('\n').join(text));

        assertEquals(getPropertyAnnotation(definition, ""), "comment");
        assertEquals(getPropertyAnnotation(definition, "@multiline"), "true\n check");
        assertEquals(getPropertyAnnotation(definition, "@type"), "text");
        assertEquals(getPropertyAnnotation(definition, "@none"), null);
    }

    @Test
    public void testGetPropertyAnnotationDuplicates() throws Exception {
        String[] text = {
            " @attr value1",
            " @type text",
            " @attr value2",
            " @other"
        };

        PropertyDefinition definition = new PropertyDefinition("k", "v", Joiner.on('\n').join(text));

        assertEquals(getPropertyAnnotation(definition, "@attr"), "value2");
    }
}
