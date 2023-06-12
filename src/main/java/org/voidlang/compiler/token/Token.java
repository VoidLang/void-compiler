package org.voidlang.compiler.token;

import dev.inventex.octa.console.ConsoleFormat;

import java.util.Objects;

/**
 * Represents a section of the parsed source string that holds specific information of a file part.
 */
public class Token {
    /**
     * The type of the token.
     */
    private final TokenType type;

    /**
     * The value of the token.
     */
    private final String value;

    /**
     * Initialize the parsed token.
     * @param type token type
     * @param value token value
     */
    private Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Get the type of the token.
     */
    public TokenType getType() {
        return type;
    }

    /**
     * Ge the value of the token.
     */
    public String getValue() {
        return value;
    }

    /**
     * Indicate, whether this token is of the specified type.
     * @param type target type to check
     * @return true if this token is of the specified type
     */
    public boolean is(TokenType type) {
        return this.type == type;
    }

    /**
     * Determine if this token has the given type and value.
     * @param type token type
     * @param value token value
     * @return true if the type and value matches
     */
    public boolean is(TokenType type, String value) {
        return this.type == type
            && this.value.equals(value);
    }

    /**
     * Determine if this token has any of the given types.
     * @param types of target token types
     * @return true if this token has the type
     */
    public boolean is(TokenType... types) {
        for (TokenType type : types) {
            if (is(type))
                return true;
        }
        return false;
    }

    /**
     * Indicate, whether this token is not a finish token.
     * @return true if there are more tokens to be parsed
     */
    public boolean hasNext() {
        return type != TokenType.UNEXPECTED
            && type != TokenType.FINISH;
    }

    /**
     * Get the string representation of this token.
     * @return token debug information
     */
    @Override
    public String toString() {
        return ConsoleFormat.YELLOW + type.name() + ConsoleFormat.DARK_GRAY + '|'
             + ConsoleFormat.WHITE + value + ConsoleFormat.DARK_GRAY + '|'
             + ConsoleFormat.WHITE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Token token = (Token) o;
        if (type != token.type)
            return false;

        return Objects.equals(value, token.value);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    /**
     * Create a new token with the specified type and value.
     * @param type token type
     * @param value token value
     * @return new parsed token
     */
    public static Token of(TokenType type, String value) {
        return new Token(type, value);
    }

    /**
     * Create a new token with the specified type.
     * @param type token type
     * @return new parsed token
     */
    public static Token of(TokenType type) {
        return new Token(type, "");
    }
}
