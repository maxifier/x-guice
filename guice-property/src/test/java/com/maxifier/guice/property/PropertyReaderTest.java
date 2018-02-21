/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import com.google.common.base.Joiner;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import static com.maxifier.guice.property.PropertyModule.loadProperties;

/**
 * @author Konstantin Lyamshin (2017-04-03 22:13)
 */
public class PropertyReaderTest extends org.testng.Assert {

    @DataProvider
    Object[][] provideComments() {
        return new String[][] {
            { "", null },
            { "#", "\n" },
            { "#\n", "\n" },
            { "no-comment", null },
            { "# basic\n", " basic\n" },
            { "\t \f # strip whitespaces\t and tabs\n", " strip whitespaces\t and tabs\n" },
            { "# # multi #mark\n", " # multi #mark\n" },
            { "! other mark! and eof", " other mark! and eof\n" },
            { "# one line\n# next line\n", " one line\n next line\n" },
            { "# differe\\n\\t \\\\escape\\\n", " differe\\n\\t \\\\escape\\\n" },
            { "# unicode\\u3aBc\uABCD\n", " unicode\u3abc\uABCD\n" },
            { "# different nl\r next", " different nl\n" },
            { "# different nl\r\n next", " different nl\n" },
            { "# different nl\n\r next", " different nl\n" },
            { "# escape and eof\\", " escape and eof\\\n" },
        };
    }

    @Test(dataProvider = "provideComments")
    public void testReadComment(String text, String expected) throws Exception {
        PropertyReader reader1 = new PropertyReader(new StringReader(text));
        assertEquals(reader1.readComment(), expected);

        PropertyReader reader2 = new PropertyReader(toStream(text));
        assertEquals(reader2.readComment(), expected);
    }

    private InputStream toStream(String text) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < sb.length(); i++) {
            if ((sb.charAt(i) & ~0xFF) != 0) {
                sb.replace(i, i + 1, String.format("\\u%4X", (int) sb.charAt(i)));
            }
        }

        return new ByteArrayInputStream(sb.toString().getBytes("ISO-8859-1"));
    }

    @DataProvider
    Object[][] provideKeys() {
        return new String[][] {
            { "", null },
            { " # ", null },
            { " ! ", null },
            { "basic", "basic" },
            { "\t \fstrip\f\t val", "strip" },
            { "stop := val", "stop" },
            { "e\\scape\\:\\=name: val", "escape:=name" },
            { "special\\t\\nchars: val", "special\t\nchars" },
            { "unicode\\uCaB0\u9876 v", "unicode\uCAB0\u9876" },
            { "strangeNL\rx", "strangeNL" },
            { "strangeNL\nx", "strangeNL" },
            { "strangeNL\r\nx", "strangeNL" },
            { "escape_eof\\", "escape_eof" },
        };
    }

    @Test(dataProvider = "provideKeys")
    public void testReadKey(String text, String expected) throws Exception {
        PropertyReader reader1 = new PropertyReader(new StringReader(text));
        assertEquals(reader1.readKey(), expected);

        PropertyReader reader2 = new PropertyReader(toStream(text));
        assertEquals(reader2.readKey(), expected);
    }

    @DataProvider
    Object[][] provideValues() {
        return new String[][] {
            { "", "" },
            { "\n", "" },
            { "#\n", "#" },
            { "!\n", "!" },
            { "eof", "eof" },
            { "val\nk", "val" },
            { "spaces: and=\n", "spaces: and=" },
            { "escape\\:\\= \\t \\n \\r\nn", "escape:= \t \n \r" },
            { "unicode\\uabC0\uC0BA\n", "unicode\uabC0\uC0BA" },
            { "strangeNL\rx", "strangeNL" },
            { "strangeNL\r\nx", "strangeNL" },
            { "strangeNL\n\rx", "strangeNL" },
            { "multi\\\nline\n", "multiline" },
            { "multi\\\n  \r\f \\\r strip", "multistrip" },
            { "multi\\\r \\\r\n strange", "multistrange" },
            { "non multi\\\\\nline", "non multi\\" },
            { "escape \\\n  \\\\\nafter multi", "escape \\" },
            { "escape eof\\", "escape eof" },
        };
    }

    @Test(dataProvider = "provideValues")
    public void testReadValue(String text, String expected) throws Exception {
        PropertyReader reader1 = new PropertyReader(new StringReader(text));
        assertEquals(reader1.readValue(), expected);

        PropertyReader reader2 = new PropertyReader(toStream(text));
        assertEquals(reader2.readValue(), expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentBadEncoding1() throws Exception {
        testReadComment("# \\u333", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentBadEncoding2() throws Exception {
        testReadComment("# \\uDEFG", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testKeyBadEncoding1() throws Exception {
        testReadKey("key\\u333", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testKeyBadEncoding2() throws Exception {
        testReadKey("key\\uDEFG", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValueBadEncoding1() throws Exception {
        testReadValue("val\\u333", "");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValueBadEncoding2() throws Exception {
        testReadValue("val\\uDEFG", "");
    }

    @Test
    public void testLoadProperties() throws Exception {
        String[] text = {
            "",
            "one\\ line property\\\\",
            "# comment",
            "",
            "# ",
            "# @multiline true",
            "# check",
            "# @type text",
            "multi\\\tline = property\\",
            "   next li\\ne \\uAbC3",
            "# eof comment",
        };

        List<PropertyDefinition> definitions = loadProperties(new StringReader(Joiner.on('\n').join(text)));
        assertEquals(definitions.size(), 2);

        HashMap<String, PropertyDefinition> props = new HashMap<String, PropertyDefinition>();
        for (PropertyDefinition definition : definitions) {
            props.put(definition.getName(), definition);
        }

        assertEquals(props.get("one line").getValue(), "property\\");
        assertEquals(props.get("one line").getComment(), "");

        assertEquals(props.get("multi\tline").getValue(), "propertynext li\ne \uAbC3");
        assertEquals(props.get("multi\tline").getComment(), " comment\n \n @multiline true\n check\n @type text\n");
    }
}
