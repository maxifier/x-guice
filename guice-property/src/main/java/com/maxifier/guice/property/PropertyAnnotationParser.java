/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import java.io.IOException;
import java.io.StringReader;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Parses property comment and extracts {@code @annotation value} pairs.
 * <p>Annotations declared in comments in form {@code @annotation value}. Values are counted from first space till
 * next annotation declaration or end of comment. Annotation declaration should be first non-whitespace character
 * in comment line.</p>
 * <pre>
 *     # example.properties
 *
 *     # annotated property example
 *     # @type any value here
 *     # @since another value here
 *     baz=baz
 * </pre>
 *
 * @author Konstantin Lyamshin (2015-11-24 14:13)
 */
class PropertyAnnotationParser implements Iterator<Map.Entry<String, String>> {
    private final PropertyReader reader;

    PropertyAnnotationParser(String comment) {
        try {
            this.reader = new PropertyReader(new StringReader(comment));
            while (reader.isWhitespaceOrNL() && reader.next()) {
                // skip trailing spaces
            }
        } catch (IOException e) {
            throw new AssertionError(e); // will never happen
        }
    }

    @Override
    public boolean hasNext() {
        return !reader.isEof();
    }

    @Override
    public Map.Entry<String, String> next() {
        if (reader.isEof()) {
            throw new NoSuchElementException();
        }

        try {
            String key = "";
            if (reader.peek() == '@') {
                key = reader.readKey();
            }

            StringBuilder value = new StringBuilder();
            do {
                String line = reader.readValue();
                if (value.length() > 0) {
                    value.append('\n'); // normalize NLs
                }
                value.append(line.trim());
                while (reader.isWhitespaceOrNL() && reader.next()) {
                    // skip whitespaces and empty lines
                }
            } while (reader.peek() != '@' && !reader.isEof()); // until next annotation

            return new AbstractMap.SimpleImmutableEntry<String, String>(key, value.toString());
        } catch (IOException e) {
            throw new AssertionError(e); // will never happen
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
