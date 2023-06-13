package org.voidlang.compiler.node.type.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.QualifiedName;
import org.voidlang.compiler.node.type.array.Array;
import org.voidlang.compiler.node.type.generic.GenericArgument;
import org.voidlang.compiler.node.type.generic.GenericArgumentList;
import org.voidlang.compiler.token.Token;

import java.util.List;

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
@AllArgsConstructor
public class ScalarType implements Type {
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

    /**
     * Indicate, whether this type is an array.
     * @return true if there are array dimensions specified
     */
    public boolean isArray() {
        return !array.getDimensions().isEmpty();
    }
}
