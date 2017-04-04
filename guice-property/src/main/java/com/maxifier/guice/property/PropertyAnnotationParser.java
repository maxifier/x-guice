/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.maxifier.guice.property.PropertyReader.*;

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
    private final String comment;
    private int pos;

    PropertyAnnotationParser(String comment) {
        this.comment = comment;
        for (pos = 0; pos < comment.length(); pos++) { // skip trailing spaces
            if (!isWhitespace(comment.charAt(pos))) {
                break;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return pos < comment.length();
    }

    @Override
    public Map.Entry<String, String> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // key
        char c = comment.charAt(pos);
        int keyFrom = pos, keyTo = pos;
        if (c == '@') {
            for (; pos < comment.length(); pos++) {
                c = comment.charAt(pos);
                if (isWhitespaceOrNL(c)) {
                    break;
                }
            }
            keyTo = pos;
        }

        // delimiter
        for (; pos < comment.length(); pos++) {
            c = comment.charAt(pos);
            if (!isWhitespace(c)) {
                break;
            }
        }

        // value
        int valFrom = pos, valTo = pos;
        do {
            for (; pos < comment.length(); pos++) { // skip line
                c = comment.charAt(pos);
                if (isNL(c)) {
                    break;
                }
                if (!isWhitespace(c)) { // trim value
                    valTo = pos + 1;
                }
            }
            for (; pos < comment.length(); pos++) { // leading space
                c = comment.charAt(pos);
                if (!isWhitespaceOrNL(c)) {
                    break;
                }
            }
        } while (c != '@' && pos < comment.length()); // until next annotation

        return new AbstractMap.SimpleImmutableEntry<String, String>(
            comment.substring(keyFrom, keyTo),
            comment.substring(valFrom, valTo)
        );
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
