/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Internal property file parser.
 * <p>Reads not only properties values but them comments too.</p>
 * <p>Comments go before property {@code key=value} pair. Comment lines starts with '#' and finished by new line char.
 * New line char leaved in comment to preserve formatting, '#' are stripped.</p>
 * <p>Annotations can be declared in comments in form {@code @annotation value}. Values are counted from first space till
 * next annotation declaration or end of comment. Annotation declaration should be first non-whitespace character
 * in comment line. Method {@link PropertyModule#getPropertyAnnotation(PropertyDefinition, String)} used to parse annotations.</p>
 * <pre>
 *     # Property 'foo' comment.
 *     # Lines preserved.
 *
 *     # Several comments concatenated
 *
 *     foo=bar
 *
 *     # annotated property example
 *     # @type any value here
 *     # @since another value here
 *     baz=baz
 * </pre>
 *
 * @author Konstantin Lyamshin (2015-11-24 14:13)
 */
class PropertyReader {
    final StringBuilder res = new StringBuilder(1024);
    final InputStream inputStream;
    final byte[] bytes;
    final Reader reader;
    final char[] chars;
    int len;
    int pos;

    PropertyReader(Reader reader) throws IOException {
        this.inputStream = null;
        this.bytes = null;

        this.reader = reader;
        this.chars = new char[8192];

        this.len = reader.read(chars);
        this.pos = 0;
    }

    PropertyReader(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.bytes = new byte[8192];

        this.reader = null;
        this.chars = null;

        this.len = inputStream.read(bytes);
        this.pos = 0;
    }

    boolean isEof() {
        return len <= 0;
    }

    private char next() throws IOException {
        if (++pos >= len) {
            pos = 0;
            if (len <= 0) { // eof on previous step
                return '\0';
            }
            len = inputStream != null
                ? inputStream.read(bytes)
                : reader.read(chars);
            if (len <= 0) { // eof reached
                return '\0';
            }
        }

        return inputStream != null
            ? (char) (0xff & bytes[pos]) // equivalent to calling a ISO8859-1 decoder
            : chars[pos];
    }

    private char peek() throws IOException {
        pos--; // little hack :)
        return next();
    }

    private char unescapeUnicode() throws IOException {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            char c = next();
            switch (c) {
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    value = (value << 4) + c - '0';
                    break;

                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                    value = (value << 4) + 10 + c - 'a';
                    break;

                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                    value = (value << 4) + 10 + c - 'A';
                    break;

                default:
                    throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
            }
        }
        return (char) value;
    }

    private char unescape(char c) throws IOException {
        switch (c) {
            case 'u': c = unescapeUnicode(); break;
            case 't': c = '\t'; break;
            case 'f': c = '\f'; break;
            case 'r': c = '\r'; break;
            case 'n': c = '\n'; break;
        }
        return c;
    }

    static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\f';
    }

    static boolean isNL(char c) {
        return c == '\n' || c == '\r';
    }

    static boolean isWhitespaceOrNL(char c) {
        return isWhitespace(c) || isNL(c);
    }

    static boolean isComment(char c) {
        return c == '#' || c == '!';
    }

    static boolean isDelimiter(char c) {
        return c == ':' || c == '=';
    }

    String readComment() throws IOException {
        boolean hasComment = false;
        res.setLength(0);

        char c = peek();
        while (!isEof()) { // loop on comment lines
            while (isWhitespaceOrNL(c)) {
                c = next(); // skip whitespaces and empty lines
            }

            if (!isComment(c)) {
                break; // non comment line
            }

            hasComment = true;
            c = next(); // skip comment mark
            while (!isEof() && !isNL(c)) { // loop on comment chars
                if (c == '\\') {
                    c = next(); // skip slash
                    if (c == 'u') {
                        c = unescapeUnicode();
                    } else {
                        res.append('\\'); // don't process escapes in comments
                    }
                    continue;
                }
                res.append(c);
                c = next();
            }
            res.append('\n'); // normalize new line char
        }

        return hasComment? res.toString(): null;
    }

    String readKey() throws IOException {
        res.setLength(0);

        char c = peek();
        while (isWhitespaceOrNL(c)) {
            c = next(); // skip whitespaces and empty lines
        }

        if (isEof()) {
            return null; // nothing read
        }

        if (isComment(c)) {
            return null; // comment line
        }

        while (!isEof() && !isDelimiter(c) && !isWhitespaceOrNL(c)) {
            if (c == '\\') {
                c = next(); // skip slash
                if (isEof()) {
                    break;
                }
                res.append(unescape(c));
                c = next();
                continue;
            }

            res.append(c);
            c = next();
        }

        return res.toString();
    }

    String readValue() throws IOException {
        res.setLength(0);

        char c = peek();
        while (isWhitespace(c) || isDelimiter(c)) {
            c = next(); // skip delimiters
        }

        while (!isEof() && !isNL(c)) {
            if (c == '\\') {
                c = next(); // skip slash
                if (isEof()) {
                    break;
                }
                if (isNL(c)) { // multi-line property value
                    while (isWhitespaceOrNL(c)) { // skip white spaces and empty lines
                        c = next();
                    }
                    continue;
                }

                res.append(unescape(c));
                c = next();
                continue;
            }

            res.append(c);
            c = next();
        }

        return res.toString();
    }
}
