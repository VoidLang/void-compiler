package org.voidlang.compiler.node.type.core;

import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.token.Token;
import org.voidlang.compiler.token.TokenType;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents an entry which may be a {@link ScalarType} or a {@link TypeGroup}.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     float foo
 *     (int, bool)
 *     int |float, string|
 * } </pre>
 * The code {@code float} will be a {@link ScalarType}, as it does not have any members, and {@code (int, bool)}
 * will be a {@link TypeGroup}, as it has two members inside.
 * @see ScalarType
 * @see TypeGroup
 */
public interface Type {
    /**
     * Indicate, whether this entry is a {@link ScalarType}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    default boolean isScalar() {
        return this instanceof ScalarType;
    }

    /**
     * Indicate, whether this entry is a {@link TypeGroup}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    default boolean isGroup() {
        return this instanceof TypeGroup;
    }

    /**
     * Indicate, whether this entry is a {@link LambdaType}, so it has nested parameter types.
     * @return true if this type entry is a callable lambda function
     */
    default boolean isLambda() {
        return this instanceof LambdaType;
    }

    /**
     * Create a new type wrapper for the specified primitive type.
     * @param type primitive type name
     * @return primitive type wrapper
     */
    static Type primitive(String type) {
        return new ScalarType(
            QualifiedName.primitive(type),
            GenericArgumentList.implicit(),
            Array.noArray()
        );
    }
}
