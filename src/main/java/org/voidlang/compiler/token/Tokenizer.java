package org.voidlang.compiler.token;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.util.Error;

import java.io.File;

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
     * The file that is being parsed.
     */
    private final File file;

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

        // handle one line comments
        if (peek() == '/' && at(cursor + 1) == '/') {
            while (peek() != '\n') {
                get();
            }
            lineIndex++;
        }

        // handle multiline comments
        else if (peek() == '/' && at(cursor + 1) == '*') {
            // skip the comment prefix
            skip(2);

            while (true) {
                if (peek() == '*' && at(cursor + 1) == '/') {
                    // skip the comment suffix
                    skip(2);
                    break;
                }
                get();
            }
        }

        // epic workaround for ignoring comments

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

        syntaxError(Error.INVALID_TOKEN, "unexpected token: `" + peek() + "`");
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

        // handle unsigned number literal
        if (peek() == 'u' && isNumber(at(cursor + 1))) {
            get();

            Token token = nextNumber();

            TokenType type = token.getType();
            String value = token.getValue();

            type = switch (type) {
                case BYTE -> TokenType.UBYTE;
                case SHORT -> TokenType.USHORT;
                case INTEGER -> TokenType.UINTEGER;
                case LONG -> TokenType.ULONG;
                default -> {
                    syntaxError(Error.INVALID_TOKEN, "invalid unsigned number literal: `" + value + "`");
                    yield TokenType.UNEXPECTED;
                }
            };

            return makeToken(type, value);
        }

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
        type = switch (c) {
            case ';' -> TokenType.SEMICOLON;
            case ':' -> TokenType.COLON;
            case ',' -> TokenType.COMMA;
            case '{' -> TokenType.BEGIN;
            case '}' -> TokenType.END;
            case '(' -> TokenType.OPEN;
            case ')' -> TokenType.CLOSE;
            case '[' -> TokenType.START;
            case ']' -> TokenType.STOP;
            default -> type;
        };
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

        // handle hexadecimal number format
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

        // handle binary number format
        else if (peek() == '0' && at(cursor + 1) == 'b') {
            // skip the '0b' prefix
            skip(2);
            // handle number content
            while (isBinary(peek()))
                get();
            // make the hexadecimal number token
            String value = range(begin, cursor);
            return makeToken(TokenType.BINARY, value);
        }

        // handle regular number
        while (isNumberContent(upper(peek()))) {
            // handle floating point number
            if (peek() == '.') {
                // check if the floating-point number contains multiple dot symbols
                if (!integer) {
                    tokenLineIndex += cursor - begin;
                    syntaxError(
                        Error.MULTIPLE_DECIMAL_POINTS,
                        "floating point number cannot have multiple dot symbols"
                    );
                    return makeToken(TokenType.UNEXPECTED);
                }
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
                    tokenLineIndex += cursor - begin - 2;
                    syntaxError(
                        Error.CANNOT_HAVE_DECIMAL_POINT,
                        "`" + type.name().toLowerCase() + "` type cannot have floating-point data"
                    );
                    return makeToken(TokenType.UNEXPECTED);
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
                        else {
                            tokenLineIndex += content.length() + 1;
                            syntaxError(Error.INVALID_ESCAPE_SEQUENCE, "invalid escape sequence: `\\" + peek() + "`");
                        }
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

        syntaxError(
            Error.MISSING_STRING_TERMINATOR,
            "missing trailing `" + (string ? '"' : '\'') + "` symbol to terminate the " + (string ? "string" : "char") + " literal"
        );
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
        return switch (c) {
            case ' ', '\t', '\r', '\n' -> true;
            default -> false;
        };
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
        return switch (c) {
            case 'B', 'S', 'I', 'L', 'F', 'D' -> true;
            default -> false;
        };
    }

    /**
     * Check if the given character is a hexadecimal number part.
     * @param c target character to test
     * @return true if the character is a hexadecimal char
     */
    private boolean isHexValue(char c) {
        return switch (c) {
            case 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> isNumber(c);
        };
    }

    /**
     * Check if the given character is a binary number part.
     * @param c target character to test
     * @return true if the character is a binary char
     */
    private boolean isBinary(char c) {
        return switch (c) {
            case '0', '1' -> true;
            default -> false;
        };
    }

    /**
     * Check if the given character is a content of a number.
     * @param c target character to test
     * @return true if the character is a number content
     */
    private boolean isNumberContent(char c) {
        return switch (c) {
            case '.', '_' -> true;
            default -> isHexValue(c) || isNumberSuffix(c);
        };
    }

    /**
     * Check if the given character is an operator.
     * @param c target character to test
     * @return true if the character is an operator
     */
    private boolean isOperator(char c) {
        return switch (c) {
            case '.', '=', '+', '-', '*', '/', '<', '>', '?', '!', '^', '&', '~', '$', '|', '%' -> true;
            default -> false;
        };
    }

    /**
     * Check if the given character is a separator.
     * @param c target character to test
     * @return true if the character is a separator
     */
    private boolean isSeparator(char c) {
        return switch (c) {
            case ';', ':', ',', '{', '}', '(', ')', '[', ']' -> true;
            default -> false;
        };
    }

    /**
     * Check if the given token is an expression token.
     * @param token target token to test
     * @return true if the token is an expression
     */
    private boolean isExpression(String token) {
        return switch (token) {
            case "new", "class", "enum", "union", "struct", "interface", "for", "while", "repeat", "do",
                "if", "else", "switch", "case", "loop", "continue", "break", "return", "await", "goto",
                "is", "in", "as", "where", "defer", "assert", "sizeof", "malloc", "free" -> true;
            default -> false;
        };
    }

    /**
     * Check if the given token is a type token.
     * @param token target token to test
     * @return true if the token is a type
     */
    private boolean isType(String token) {
        return switch (token) {
            case "let", "mut", "ref", "deref", "byte", "ubyte", "short", "ushort", "int", "uint", "double", "udouble",
                "float", "ufloat", "long", "ulong", "void", "bool", "char", "string" -> true;
            default -> false;
        };
    }

    /**
     * Check if the given token is a modifier token.
     * @param token target token to test
     * @return true if the token is a modifier
     */
    private boolean isModifier(String token) {
        return switch (token) {
            case "public", "protected", "private", "static", "final", "native", "extern", "transient",
                "synchronized", "async", "const", "unsafe", "weak", "strong", "default" -> true;
            default -> false;
        };
    }

    /**
     * Check if the given token is a boolean token.
     * @param token target token to test
     * @return true if the token is a boolean
     */
    private boolean isBoolean(String token) {
        return switch (token) {
            case "true", "false" -> true;
            default -> false;
        };
    }

    /**
     * Check if the given token is an information token.
     * @param token target token to test
     * @return true if the token is an information
     */
    private boolean isInfo(String token) {
        return switch (token) {
            case "package", "import", "using" -> true;
            default -> false;
        };
    }

    /**
     * Check if the given token is a null token.
     * @param token target token to test
     * @return true if the token is a null
     */
    private boolean isNull(String token) {
        return switch (token) {
            case "null", "nullptr" -> true;
            default -> false;
        };
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
    private void syntaxError(Error error, String message) {
        System.err.println(ConsoleFormat.RED + "error[E" + error.getCode() + "]" + ConsoleFormat.WHITE + ": " + message);
        System.err.println(ConsoleFormat.CYAN + " --> " + ConsoleFormat.LIGHT_GRAY + file.getName() + ":" + tokenLineNumber + ":" + tokenLineIndex);

        int lineSize = String.valueOf(tokenLineNumber).length();

        // display the line number
        System.err.print(ConsoleFormat.CYAN + " ".repeat(lineSize + 1));
        System.err.println(" | ");

        System.err.print(" " + tokenLineNumber + " | ");

        // get the line of the error
        String line = data.split("\n")[tokenLineNumber - 1];
        // get the start and end index of the line
        int start = Math.max(0, tokenLineIndex - MAX_ERROR_LINE_LENGTH);
        int end = Math.min(line.length(), tokenLineIndex + MAX_ERROR_LINE_LENGTH);

        // display the line of the error
        System.err.println(ConsoleFormat.LIGHT_GRAY + line.substring(start, end));
        // display the error pointer
        System.err.print(ConsoleFormat.CYAN + " ".repeat(lineSize + 1));
        System.err.println(" | " + " ".repeat(lineSize + (tokenLineIndex - start) - 1) + ConsoleFormat.RED + "^");
        // exit the program with the error code

        System.exit(error.getCode());
    }
}
