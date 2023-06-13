package org.voidlang.compiler.node.type.generic;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.voidlang.compiler.node.type.core.Type;

import java.util.List;

/**
 * Represents a generic type argument that is explicitly declared when using a type with generics.
 * Generic arguments may have nested members as well that are handled recursively.
 * <p>Example:</p>
 * <pre> {@code
 *     TypeWithGeneric<GenericArgument<NestedArgument1, NestedArgument2>, OtherGenericArgument>
 * } </pre>
 */
@AllArgsConstructor
public class GenericArgument {
    /**
     * the type of the generic argument.
     */
    @NotNull
    private final Type type;

    /**
     * The members of the generic argument. It is null if no inner type arguments were given.
     * If the diamond operator is used, this list is an empty list.
     */
    @Nullable
    private final List<GenericArgument> members;
}
