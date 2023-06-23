package org.voidlang.compiler.node;

/**
 * Represents a registry of the parsable node types.
 */
public enum NodeType {
    ROOT,
    MODIFIER_LIST,
    MODIFIER_BLOCK,
    METHOD,
    FIELD,
    MULTI_FIELD,
    PACKAGE,
    IMPORT,
    CLASS,
    STRUCT,
    TUPLE_STRUCT,
    ENUM,
    INTERFACE,
    ANNOTATION,
    LOCAL_DECLARE,
    MULTI_LOCAL_DECLARE,
    LOCAL_DECLARE_ASSIGN,
    LOCAL_DECLARE_DESTRUCTURE_TUPLE,
    LOCAL_DECLARE_DESTRUCTURE_STRUCT,
    LOCAL_ASSIGN,
    VALUE,
    ACCESSOR,
    PARAMETER_INDEXER,
    NEW,
    NAME,
    COMPOUND_NAME,
    INITIALIZATOR,
    OPERATION,
    JOIN_OPERATION,
    SIDE_OPERATION,
    METHOD_CALL,
    GROUP,
    TEMPLATE,
    LAMBDA,
    INDEX_FETCH,
    INDEX_ASSIGN,
    RETURN,
    DEFER,
    TUPLE,
    IF,
    ELSE_IF,
    ELSE,
    WHILE,
    DO_WHILE,
    FOR,
    FOR_EACH,
    EMPTY,
    ERROR,
    FINISH
}
