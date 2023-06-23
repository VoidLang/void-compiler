package org.voidlang.compiler.node.type.core;

import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRType;

/**
 * Represents an entry which may be a {@link ScalarType} or a {@link CompoundType}.
 * The purpose of this class is to be able to hold type groups <strong>recursively</strong>.
 * <p>Examples:</p>
 * <pre> {@code
 *     float foo
 *     (int, bool)
 *     int |float, string|
 * } </pre>
 * The code {@code float} will be a {@link ScalarType}, as it does not have any members, and {@code (int, bool)}
 * will be a {@link CompoundType}, as it has two members inside.
 * @see ScalarType
 * @see CompoundType
 */
public abstract class Type {
    /**
     * The type wrapper for the "let" keyword.
     */
    public static final Type LET = primitive("let");

    /**
     * The type wrapper for an 8-bit integer.
     */
    public static final Type BYTE = primitive("byte");

    /**
     * The type wrapper for a 16-bit integer.
     */
    public static final Type SHORT = primitive("short");

    /**
     * The type wrapper for a 32-bit integer.
     */
    public static final Type INT = primitive("int");

    /**
     * The type wrapper for a 64-bit integer.
     */
    public static final Type LONG = primitive("long");
    
    /**
     * Indicate, whether this entry is a {@link ScalarType}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    public boolean isScalar() {
        return this instanceof ScalarType;
    }

    /**
     * Indicate, whether this entry is a {@link CompoundType}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    public boolean isCompound() {
        return this instanceof CompoundType;
    }

    /**
     * Indicate, whether this entry is a {@link LambdaType}, so it has nested parameter types.
     * @return true if this type entry is a callable lambda function
     */
    public boolean isLambda() {
        return this instanceof LambdaType;
    }

    /**
     * Create a new type wrapper for the specified primitive type.
     * @param type primitive type name
     * @return primitive type wrapper
     */
    public static Type primitive(String type) {
        return new ScalarType(
            QualifiedName.primitive(type),
            GenericArgumentList.implicit(),
            Array.noArray()
        );
    }

    /**
     * Generate an LLVM type for this type wrapper
     * @param context LLVM module context
     * @return type ir code wrapper
     */
    public abstract IRType generateType(IRContext context);
}
