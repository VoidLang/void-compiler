package org.voidlang.compiler.node.type.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.llvm.element.Builder;
import org.voidlang.llvm.element.Value;

/**
 * Represents a type use in the Void syntax that has a type token, generic arguments and array dimensions.
 * <p>Example:</p>
 * <pre> {@code
 *     MyCollection.Document<User>[] documents
 * } </pre>
 * Here {@code MyCollection.Document} is the type token, {@code <User>} is the generic token, and
 * {@code []} is a one-dimensional array specifier.
 */
@Getter
public class ScalarType extends Type {
    /**
     * The fully qualified name of the type.
     */
    @NotNull
    private final QualifiedName name;

    /**
     * The generic argument tokens of the type.
     */
    @NotNull
    private final GenericArgumentList generics;

    /**
     * The dimensions of the multidimensional array.
     */
    @NotNull
    private final Array array;

    public ScalarType(@NotNull QualifiedName name, @NotNull GenericArgumentList generics,
                      @NotNull Array array) {
        this.name = name;
        this.generics = generics;
        this.array = array;
    }

    /**
     * Indicate, whether this type is an array.
     * @return true if there are array dimensions specified
     */
    public boolean isArray() {
        return !array.getDimensions().isEmpty();
    }

    /**
     * Get the string representation of the scalar type.
     * @return scalar type debug information
     */
    @Override
    public String toString() {
        return String.valueOf(name) + generics + array;
    }

    /**
     * Generate an LLVM instruction for this node
     * @param builder instruction builder for the current context
     * @return node ir code wrapper
     */
    @Override
    public Value generate(Builder builder) {
        return null;
    }
}
