/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.guice.property;

import javax.annotation.Nonnull;
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
    private static final int REPLACEMENT_CHAR = '\uFFFD';

    private final StringBuilder res = new StringBuilder(1024);
    private final InputStream inputStream;
    private final byte[] bytes;
    private final Reader reader;
    private final char[] chars;
    private int len; // buffer len
    private int pos; // buffer pos
    private int c; // fetched char

    /**
     * Read properties from character stream
     */
    PropertyReader(Reader reader) throws IOException {
        this.inputStream = null;
        this.bytes = null;

        this.reader = reader;
        this.chars = new char[8192];

        this.c = nextChar();
    }

    /**
     * Read properties from byte stream (imply UTF-8 encoding)
     */
    PropertyReader(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.bytes = new byte[8192];

        this.reader = null;
        this.chars = null;

        this.c = nextByte();
    }

    private boolean hasNext() throws IOException {
        if (len < 0) {
            return true;
        }
        if (pos < len) {
            return false;
        }

        pos = 0;
        do {
            len = inputStream != null
                ? inputStream.read(bytes)
                : reader.read(chars);
        } while (len == 0);

        return len < 0;
    }

    int peek() {
        return c;
    }

    boolean next() throws IOException {
        c = inputStream != null? nextByte(): nextChar();
        return c != -1;
    }

    /**
     * Read next code point from stream using UTF-8 encoding
     */
    private int nextByte() throws IOException {
        if (hasNext()) {
            return -1;
        }
        byte b1 = bytes[pos++];
        if ((b1 & 0x80) == 0) { // single byte
            return checkCodePoint(b1 & 0x7F);
        }

        if (hasNext()) {
            return REPLACEMENT_CHAR;
        }
        byte b2 = bytes[pos++];
        if ((b2 & 0xC0) != 0x80) {
            pos--; // bring back unexpected byte
            return REPLACEMENT_CHAR;
        }
        if ((b1 & 0xE0) == 0xC0) { // two bytes
            return checkCodePoint((b1 & 0x1F) << 6 | (b2 & 0x3F));
        }

        if (hasNext()) {
            return REPLACEMENT_CHAR;
        }
        byte b3 = bytes[pos++];
        if ((b3 & 0xC0) != 0x80) {
            pos--; // bring back unexpected byte
            return REPLACEMENT_CHAR;
        }
        if ((b1 & 0xF0) == 0xE0) { // three bytes
            return checkCodePoint((b1 & 0x0F) << 12 | (b2 & 0x3F) << 6 | (b3 & 0x3F));
        }

        if (hasNext()) {
            return REPLACEMENT_CHAR;
        }
        byte b4 = bytes[pos++];
        if ((b4 & 0xC0) != 0x80) {
            pos--; // bring back unexpected byte
            return REPLACEMENT_CHAR;
        }
        if ((b1 & 0xF8) == 0xF0) { // four bytes
            return checkCodePoint((b1 & 0x07) << 18 | (b2 & 0x3F) << 12 | (b3 & 0x3F) << 6 | (b4 & 0x3F));
        }

        return REPLACEMENT_CHAR;
    }

    private static int checkCodePoint(int cp) {
        return Character.isValidCodePoint(cp)? cp: REPLACEMENT_CHAR;
    }

    private int nextChar() throws IOException {
        if (hasNext()) {
            return -1;
        }
        char c1 = chars[pos++];
        if (!Character.isHighSurrogate(c1)) { // BMP char
            return checkCodePoint(c1);
        }

        if (hasNext()) {
            return REPLACEMENT_CHAR;
        }
        char c2 = chars[pos++];
        if (!Character.isLowSurrogate(c2)) {
            pos--; // bring back unexpected char
            return REPLACEMENT_CHAR;
        }

        return Character.toCodePoint(c1, c2);
    }

    private boolean unescape() throws IOException {
        next(); // skip slash
        switch (c) {
            case 'u': unescapeUnicode(); break;
            case 't': c = '\t'; break;
            case 'f': c = '\f'; break;
            case 'r': c = '\r'; break;
            case 'n': c = '\n'; break;

            case '\r': // multi-line property value
                next();

            case '\n':
                if (c == '\n') {
                    next(); // skip CRLF sequence
                }
                while (isWhitespace() && next()) {
                    // trim white spaces
                }
                return true;

            case -1: // skip slash on EOF
                return true;
        }
        return false; // do further processing
    }

    private void unescapeUnicode() throws IOException {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            next();
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
        this.c = value;
    }

    boolean isEof() {
        return c == -1;
    }

    boolean isWhitespace() {
        return c == ' ' || c == '\t' || c == '\f';
    }

    boolean isNL() {
        return c == '\n' || c == '\r';
    }

    boolean isWhitespaceOrNL() {
        return isWhitespace() || isNL();
    }

    boolean isComment() {
        return c == '#' || c == '!';
    }

    boolean isDelimiter() {
        return c == ':' || c == '=';
    }

    @Nonnull
    String readComment() throws IOException {
        res.setLength(0);

        while (isWhitespaceOrNL() && next()) {
            // skip whitespaces and empty lines
        }

        while (!isEof() && isComment()) { // loop on comment lines
            next(); // skip comment char
            while (!isEof() && !isNL()) { // loop on comment chars
                if (c == '\\') {
                    next();
                    switch (c) {
                        case 'u': unescapeUnicode(); break;
                        case '\r': // don't process NL and EOF escape
                        case '\n':
                        case -1: res.append('\\'); continue;
                        default: res.append('\\'); // don't process escapes in comments
                    }
                }
                res.appendCodePoint(c);
                next();
            }

            while (isWhitespaceOrNL() && next()) {
                // skip whitespaces and empty lines
            }

            res.append('\n'); // normalize new line char
        }

        return res.toString();
    }

    @Nonnull
    String readKey() throws IOException {
        res.setLength(0);

        while (isWhitespaceOrNL() && next()) {
            // skip whitespaces and empty lines
        }

        while (!isEof() && !isDelimiter() && !isWhitespaceOrNL()) {
            if (c == '\\' && unescape()) {
                continue;
            }
            res.appendCodePoint(c);
            next();
        }

        while (isWhitespace() || isDelimiter()) {
            next(); // skip delimiters, don't skip NL to process empty value correctly
        }

        return res.toString();
    }

    @Nonnull
    String readValue() throws IOException {
        res.setLength(0);

        while (!isEof() && !isNL()) {
            if (c == '\\' && unescape()) {
                continue;
            }
            res.appendCodePoint(c);
            next();
        }

        while (isNL() && next()) {
            // skip delimiters
        }

        return res.toString();
    }

}
