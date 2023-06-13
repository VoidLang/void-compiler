package org.voidlang.compiler.token;

import lombok.RequiredArgsConstructor;

/**
 * Represents a utility that parses raw string input to tokens.
 */
@RequiredArgsConstructor
public class Tokenizer {
    /**
     * The maximum length of a displayable line of code in a syntax error.
     */
    private static final int MAX_ERROR_LINE_LENGTH = 30;

    /**
     * The input data of the tokenizer.
     */
    private final String data;

    /**
     * The current index of currently parsed character.
     */
    private int cursor = 0;

    /**
     * The index of the current character in the line being processed.
     */
    private int lineIndex = 0;

    /**
     * The number of the current line being processed.
     */
    private int lineNumber = 1;

    /**
     * The index of the first character in the line of the token being processed.
     */
    private int tokenLineIndex = 0;

    /**
     * The number of the current line being processed for the token.
     */
    private int tokenLineNumber = 1;

    /**
     * The beginning index of the currently parsed token.
     */
    private int beginIndex;

    /**
     * Parse the next token from the data.
     * @return next parsed token
     */
    public Token next() {
        // ignore all whitespaces from the content
        while (isWhitespace(peek())) {
            // handle new line
            if (get() == '\n') {
                // reset the line index
                lineIndex = 0;
                lineNumber++;
                // make a new line token to be replaced later to semicolons
                return makeToken(TokenType.NEW_LINE);
            }
        }

        // handle end of file
        if (peek() == '\0')
            return makeToken(TokenType.FINISH);

        beginIndex = cursor;
        tokenLineNumber = lineNumber;
        tokenLineIndex = lineIndex;

        // handle identifiers
        if (isIdentifierStart(peek()))
            return nextIdentifier();

        // handle operators
        // TODO handle comments here, before operators
        else if (isOperator(peek()))
            return nextOperator();
        // handle separators
        else if (isSeparator(peek()))
            return nextSeparator();

        // handle numbers
        else if (isNumber(peek()))
            return nextNumber();
        // handle string literals
        else if (isString(peek()))
            return nextString();
        // handle char literals
        else if (isChar(peek()))
            return nextChar();

        // handle annotations
        else if (isAnnotation(peek()))
            return nextAnnotation();
        // handle invalid syntax

        syntaxError("");
        return makeToken(TokenType.UNEXPECTED);
    }

    /**
     * Make a new token of the specified type and value.
     * @param type token type
     * @param value token value
     * @return new token
     */
    private Token makeToken(TokenType type, String value) {
        return Token.of(type, value, new TokenMeta(beginIndex, cursor, tokenLineIndex, tokenLineNumber));
    }

    /**
     * Make a new token of the specified type.
     * @param type token type
     * @return new token
     */
    public Token makeToken(TokenType type) {
        return makeToken(type, "");
    }

    /**
     * Parse the next identifier token.
     * @return new identifier token
     */
    public Token nextIdentifier() {
        // get the full identifier
        int begin = cursor;
        while (isIdentifierPart(peek()))
            get();
        String token = range(begin, cursor);
        // determine the token type
        TokenType type = TokenType.IDENTIFIER;
        if (isExpression(token))
            type = TokenType.EXPRESSION;
        else if (isType(token))
            type = TokenType.TYPE;
        else if (isModifier(token))
            type = TokenType.MODIFIER;
        else if (isBoolean(token))
            type = TokenType.BOOLEAN;
        else if (isInfo(token))
            type = TokenType.INFO;
        else if (isNull(token))
            type = TokenType.NULL;
        // make the identifier token
        return makeToken(type, token);
    }

    /**
     * Parse the next operator token.
     * @return new operator token
     */
    public Token nextOperator() {
        return makeToken(TokenType.OPERATOR, String.valueOf(get()));
    }

    /**
     * Parse the next separator token.
     * @return new separator token
     */
    public Token nextSeparator() {
        TokenType type = TokenType.UNEXPECTED;
        char c = get();
        switch (c) {
            case ';':
                type = TokenType.SEMICOLON;
                break;
            case ':':
                type = TokenType.COLON;
                break;
            case ',':
                type = TokenType.COMMA;
                break;
            case '{':
                type = TokenType.BEGIN;
                break;
            case '}':
                type = TokenType.END;
                break;
            case '(':
                type = TokenType.OPEN;
                break;
            case ')':
                type = TokenType.CLOSE;
                break;
            case '[':
                type = TokenType.START;
                break;
            case ']':
                type = TokenType.STOP;
                break;
        }
        return makeToken(type, String.valueOf(c));
    }

    /**
     * Parse the next number token.
     * @return new number token
     */
    public Token nextNumber() {
        // get the beginning of the number content
        int begin = cursor;
        // determine if the number is integer
        boolean integer = true;

        // handle hexadecimal numbers
        if (peek() == '0' && at(cursor + 1) == 'x') {
            // skip the '0x' prefix
            skip(2);
            // handle number content
            while (isHexValue(peek()))
                get();
            // make the hexadecimal number token
            String value = range(begin, cursor);
            return makeToken(TokenType.HEXADECIMAL, value);
        }

        // handle regular number
        while (isNumberContent(upper(peek()))) {
            // handle floating point number
            if (peek() == '.') {
                // check if the floating-point number contains multiple dot symbols
                if (!integer)
                    return makeToken(TokenType.UNEXPECTED, "Floating point number cannot have multiple dot symbols.");
                integer = false;
            }

            // check if a number type suffix is specified
            if (isNumberSuffix(upper(peek()))) {
                // get the type of the number
                TokenType type = integer ? TokenType.INTEGER : TokenType.DOUBLE;
                switch (upper(peek())) {
                    case 'B':
                        type = TokenType.BYTE;
                        break;
                    case 'S':
                        type = TokenType.SHORT;
                        break;
                    case 'I':
                        type = TokenType.INTEGER;
                        break;
                    case 'L':
                        type = TokenType.LONG;
                        break;
                    case 'F':
                        type = TokenType.FLOAT;
                        break;
                    case 'D':
                        type = TokenType.DOUBLE;
                        break;
                }

                // check if integer type value has non-floating-point data
                if (!integer && (type == TokenType.BYTE || type == TokenType.SHORT
                        || type == TokenType.INTEGER || type == TokenType.LONG)) {
                    return makeToken(TokenType.UNEXPECTED, type + " cannot have a floating-point value.");
                }

                // skip the type specifier
                skip(1);
                // get the value of the number
                String value = range(begin, cursor - 1);
                return makeToken(type, value);
                // TODO check if number declaration ended because a type specifier were set, 
                //  but after the specifier there is no separator or whitespace eg. 1.5Flol
            }
            // move to the next number part
            skip(1);
        }
        // get the value of the number
        String value = range(begin, cursor);
        return makeToken(integer ? TokenType.INTEGER : TokenType.DOUBLE, value);
    }

    /**
     * Parse the next string literal token.
     * @return new string token
     */
    public Token nextString() {
        return nextLiteral(true);
    }

    /**
     * Parse the next char literal token.
     * @return new char token
     */
    public Token nextChar() {
        return nextLiteral(false);
    }

    /**
     * Parse the next string or char literal token.
     * @param string true for string, false for char
     * @return new string or char token
     */
    public Token nextLiteral(boolean string) {
        // declare the string literal content
        StringBuilder content = new StringBuilder();
        // skip the quotation mark
        skip(1);
        boolean escapeNext = false;

        // loop until the string literal is terminated or the end of file has been reached
        while (has(cursor)) {
            // handle escaped characters
            if (escapeNext) {
                switch (peek()) {
                    case 'n':
                        content.append('\n');
                        break;
                    case 'r':
                        content.append('\r');
                        break;
                    case 't':
                        content.append('\t');
                        break;
                    case '\\':
                        content.append('\\');
                        break;
                    // TODO handle \\u character code
                    default:
                        if ((string && peek() == '"') || (!string && peek() == '\''))
                            content.append(peek());
                        else
                            syntaxError("Invalid escape sequance: \\" + peek());
                }
                escapeNext = false;
            }

            // handle escaping the next character
            else if (peek() == '\\')
                escapeNext = true;

            // handle the ending of the string literal
            else if ((peek() == '"' && string) || (peek() == '\'' && !string)) {
                // skip the end of the string
                skip(1);
                return makeToken(string ? TokenType.STRING : TokenType.CHARACTER, content.toString());
            }

            // handle string literal content
            else
                content.append(peek());
            // move to the next string character
            skip(1);
        }

        syntaxError("Missing trailing `" + (string ? '"' : '\'') + "` symbol to terminate the " + (string ? "string" : "char") + " literal.");
        return makeToken(TokenType.UNEXPECTED);
    }

    /**
     * Parse the next annotation token.
     * @return new annotation token
     */
    public Token nextAnnotation() {
        // skip the '@' symbol
        skip(1);
        // parse the name of the annotation
        Token token = nextIdentifier();
        // check for errors
        if (!token.is(TokenType.IDENTIFIER))
            return token;
        // create the annotation token
        return makeToken(TokenType.ANNOTATION, token.getValue());
    }

    /**
     * Get the character at the current index.
     * @return currently parsed data index
     */
    private char peek() {
        return at(cursor);
    }

    /**
     * Get the current non-whitespace character from the data.
     * @return current non-whitespace character
     */
    private char peekNoWhitespace() {
        int index = cursor;
        while (isWhitespace(at(index)))
            index--;
        return at(index);
    }

    /**
     * Get the character at the current index and move to the next position.
     * @return currently parsed data index
     */
    private char get() {
        lineIndex++;
        return at(cursor++);
    }

    /**
     * Get the previous character from the data.
     * @return previously parsed character
     */
    private char prev() {
        return at(cursor - 1);
    }

    /**
     * Get the previous nth character from the data.
     * @param skip rewind amount
     * @return previous nth character
     */
    private char prev(int skip) {
        return at(cursor - skip);
    }

    /**
     * Move the cursor with the given amount.
     * @param amount cursor move amount
     */
    private void skip(int amount) {
        lineIndex += amount;
        cursor += amount;
    }

    /**
     * Get the character at the given index.
     * @param index target data index
     * @return character at the index or '\0' if it is out of the bounds
     */
    private char at(int index) {
        return has(index) ? data.charAt(index) : '\0';
    }

    /**
     * Determine if the given index is in bounds of the data size.
     * @param index target index to check
     * @return true if the index is in the parsed data
     */
    private boolean has(int index) {
        return index >= 0 && index < data.length();
    }

    /**
     * Get the string value from the data within the given range.
     * @param begin data range start index
     * @param end data range finish index
     */
    private String range(int begin, int end) {
        return data.substring(begin, end);
    }

    /**
     * Check if the given character is a whitespace.
     * @param c target character to test
     * @return true if the character is a whitespace
     */
    private boolean isWhitespace(char c) {
        switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given character is the beginning of an identifier.
     * @param c target character to test
     * @return true if the character is an identifier beginning
     */
    private boolean isIdentifierStart(char c) {
        return Character.isJavaIdentifierStart(c);
    }

    /**
     * Check if the given character is the part of an identifier.
     * @param c target character to test
     * @return true if the character is an identifier part
     */
    private boolean isIdentifierPart(char c) {
        return Character.isJavaIdentifierPart(c);
    }

    /**
     * Check if the given character is numeric.
     * @param c target character to test
     * @return true if the character is numeric
     */
    private boolean isNumber(char c) {
        return Character.isDigit(c);
    }

    /**
     * Check if the given character is the beginning of a string.
     * @param c target character to test
     * @return true if the character is a string beginning
     */
    private boolean isString(char c) {
        return c == '"';
    }

    /**
     * Check if the given character is the beginning of a char.
     * @param c target character to test
     * @return true if the character is a char beginning
     */
    private boolean isChar(char c) {
        return c == '\'';
    }

    /**
     * Check if the given character is the beginning of an annotation.
     * @param c target character to test
     * @return true if the character is an annotation beginning
     */
    private boolean isAnnotation(char c) {
        return c == '@';
    }

    /**
     * Check if the given character is the ending of a number.
     * @param c target character to test
     * @return true if the character is a number suffix
     */
    private boolean isNumberSuffix(char c) {
        switch (c) {
            case 'B':
            case 'S':
            case 'I':
            case 'L':
            case 'F':
            case 'D':
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given character is a hexadecimal number part.
     * @param c target character to test
     * @return true if the character is a hexadecimal char
     */
    private boolean isHexValue(char c) {
        switch (c) {
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return true;
            default:
                return isNumber(c);
        }
    }

    /**
     * Check if the given character is a content of a number.
     * @param c target character to test
     * @return true if the character is a number content
     */
    private boolean isNumberContent(char c) {
        switch (c) {
            case '.':
            case '_':
                return true;
            default:
                return isHexValue(c) || isNumberSuffix(c);
        }
    }

    /**
     * Check if the given character is an operator.
     * @param c target character to test
     * @return true if the character is an operator
     */
    private boolean isOperator(char c) {
        switch (c) {
            case '.':
            case '=':
            case '+':
            case '-':
            case '*':
            case '/':
            case '<':
            case '>':
            case '?':
            case '!':
            case '^':
            case '&':
            case '~':
            case '$':
            case '|':
            case '%':
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given character is a separator.
     * @param c target character to test
     * @return true if the character is a separator
     */
    private boolean isSeparator(char c) {
        switch (c) {
            case ';':
            case ':':
            case ',':
            case '{':
            case '}':
            case '(':
            case ')':
            case '[':
            case ']':
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given token is an expression token.
     * @param token target token to test
     * @return true if the token is an expression
     */
    private boolean isExpression(String token) {
        switch (token) {
            case "new":
            case "class":
            case "enum":
            case "struct":
            case "interface":
            case "for":
            case "while":
            case "repeat":
            case "do":
            case "if":
            case "else":
            case "switch":
            case "case":
            case "loop":
            case "continue":
            case "break":
            case "return":
            case "await":
            case "goto":
            case "is":
            case "in":
            case "as":
            case "where":
            case "defer":
            case "assert":
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given token is a type token.
     * @param token target token to test
     * @return true if the token is a type
     */
    private boolean isType(String token) {
        switch (token) {
            case "let":
            case "byte":
            case "ubyte":
            case "short":
            case "ushort":
            case "int":
            case "uint":
            case "double":
            case "udouble":
            case "float":
            case "ufloat":
            case "long":
            case "ulong":
            case "void":
            case "bool":
            case "char":
            case "string":
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given token is a modifier token.
     * @param token target token to test
     * @return true if the token is a modifier
     */
    private boolean isModifier(String token) {
        switch (token) {
            case "public":
            case "protected":
            case "private":
            case "static":
            case "final":
            case "native":
            case "extern":
            case "transient":
            case "synchronized":
            case "async":
            case "const":
            case "unsafe":
            case "weak":
            case "strong":
            case "default":
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given token is a boolean token.
     * @param token target token to test
     * @return true if the token is a boolean
     */
    private boolean isBoolean(String token) {
        switch (token) {
            case "true":
            case "false":
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given token is an information token.
     * @param token target token to test
     * @return true if the token is an information
     */
    private boolean isInfo(String token) {
        switch (token) {
            case "package":
            case "import":
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if the given token is a null token.
     * @param token target token to test
     * @return true if the token is a null
     */
    private boolean isNull(String token) {
        switch (token) {
            case "null":
            case "nullptr":
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the uppercase format of the given character.
     * @param c target character to be transformer
     * @return uppercase representation of the character
     */
    private char upper(char c) {
        return Character.toUpperCase(c);
    }

    /**
     * Get the lowercase format of the given character.
     * @param c target character to be transformer
     * @return lowercase representation of the character
     */
    private char lower(char c) {
        return Character.toLowerCase(c);
    }

    /**
     * Display a syntax error in the console with debug information.
     * @param message error message
     */
    private void syntaxError(String message) {
    }
}
