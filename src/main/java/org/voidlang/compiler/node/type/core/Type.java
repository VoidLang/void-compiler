package org.voidlang.compiler.node.type.core;

import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.NodeType;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

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
public class Type extends Node {
    public Type() {
        super(NodeType.TYPE);
    }

    /**
     * Indicate, whether this entry is a {@link ScalarType}, so it does not have any nested members.
     * @return true if this type entry is a direct type
     */
    public boolean isScalar() {
        return this instanceof ScalarType;
    }

    /**
     * Indicate, whether this entry is a {@link TypeGroup}, so it has nested members only.
     * @return true if this type entry is a group of type entries
     */
    public boolean isGroup() {
        return this instanceof TypeGroup;
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
     * The type wrapper for the "let" keyword.
     */
    public static final Type LET = primitive("let");

    /**
     * Generate an LLVM instruction for this node
     *
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
