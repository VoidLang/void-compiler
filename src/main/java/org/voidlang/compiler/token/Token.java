package org.voidlang.compiler.token;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents a section of the parsed source string that holds specific information of a file part.
 */
@AllArgsConstructor
@Getter
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
     * The meta information of the token.
     */
    private final TokenMeta meta;

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
     * Indicate, whether the token's value equals to the specified value.
     * @param values the values to test
     * @return true if this token has the value
     */
    public boolean val(String... values) {
        for (String value : values) {
            if (this.value.equals(value))
                return true;
        }
        return false;
    }

    /**
     * Indicate, whether this token is not a finish token.
     * @return true if there are more tokens to be parsed
     */
    public boolean hasNext() {
        return switch (type) {
            case UNEXPECTED, FINISH -> false;
            default -> true;
        };
    }

    /**
     * Check if this token does not match the specified type.
     * @param type first token type to check
     * @param types target token types to check
     * @return this token or an error
     */
    public Token expect(TokenType type, TokenType... types) {
        if (is(type))
            return this;
        for (TokenType test : types) {
            if (is(test))
                return this;
        }
        throw new IllegalStateException("Expected " + type + ", but got " + this);
    }

    /**
     * Check if this token does not match the specified type.
     * @param token first token to check
     * @param tokens target tokens to check
     * @return this token or an error
     */
    public Token expect(Token token, Token... tokens) {
        if (equals(token))
            return this;
        for (Token test : tokens) {
            if (equals(test))
                return this;
        }
        throw new IllegalStateException("Expected " + type + ", but got " + this);
    }

    /**
     * Determine if the type of this token is a number.
     * @return true if this token is a number
     */
    public boolean isNumber() {
        return switch (type) {
            case BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, HEXADECIMAL, BINARY -> true;
            default -> false;
        };
    }

    /**
     * Determine if the type of this token is a literal token type.
     * @return true if this token is a constant literal
     */
    public boolean isLiteral() {
        return switch (type) {
            case STRING, CHARACTER, BOOLEAN -> true;
            default -> isNumber();
        };
    }

    /**
     * Indicate, whether the value of this token is specific, and must be checked.
     * @return true if the token value should be checked
     */
    public boolean isSpecific() {
        return isLiteral() || is(TokenType.IDENTIFIER);
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
     * @param meta token metadata information
     * @return new parsed token
     */
    public static Token of(TokenType type, String value, TokenMeta meta) {
        return new Token(type, value, meta);
    }

    /**
     * Create a new token with the specified type.
     * @param type token type
     * @param meta token metadata information
     * @return new parsed token
     */
    public static Token of(TokenType type, TokenMeta meta) {
        return new Token(type, "", meta);
    }

    /**
     * Create a new token with the specified type and value.
     * @param type token type
     * @param value token value
     * @return new parsed token
     */
    public static Token of(TokenType type, String value) {
        return new Token(type, value, null);
    }

    /**
     * Create a new token with the specified type.
     * @param type token type
     * @return new parsed token
     */
    public static Token of(TokenType type) {
        return new Token(type, "", null);
    }
}
