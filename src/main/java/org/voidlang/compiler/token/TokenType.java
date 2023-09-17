package org.voidlang.compiler.token;

/**
 * Represents a registry of parsable the token types.
 */
public enum TokenType {
    STRING,      // "text"
    CHARACTER,   // 'A'
    BEGIN,       // {
    END,         // }
    BYTE,        // 12B
    UBYTE,       // u12B
    SHORT,       // 100S
    USHORT,      // u100S
    DOUBLE,      // 3.0D, 3.0
    UDOUBLE,     // u3.0D, u3.0
    FLOAT,       // 1.5F
    UFLOAT,      // u1.5F
    LONG,        // 123L
    ULONG,       // u123L
    INTEGER,     // 1337
    UINTEGER,    // u1337
    HEXADECIMAL, // 0xFFFFF
    BINARY,      // 0b01101
    BOOLEAN,     // true
    SEMICOLON,   // ;
    EXPRESSION,  // class
    COLON,       // :
    COMMA,       // ,
    OPEN,        // (
    CLOSE,       // )
    IDENTIFIER,  // abc
    OPERATOR,    // +
    TYPE,        // int
    MODIFIER,    // public
    START,       // [
    STOP,        // ]
    ANNOTATION,  // @Link
    LINE_NUMBER, // L11
    NULL,        // null
    INFO,        // file information
    FINISH,      // content finished
    UNEXPECTED,  // syntax error,
    NEW_LINE,    // temp new line, to be replaced by semicolons or be cleared
    NONE         // no such token
}
