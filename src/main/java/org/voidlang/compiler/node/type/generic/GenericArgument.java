package org.voidlang.compiler.node.type.generic;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.voidlang.compiler.node.type.core.Type;

import java.util.List;
import java.util.stream.Collectors;

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
     * The members of the generic argument. If the diamond operator is used,
     * this list is an empty list.
     */
    @NotNull
    private final List<GenericArgument> members;

    /**
     * Indicate, whether the inner type arguments were explicitly declared.
     */
    private final boolean explicit;

    /**
     * Get the string representation of the qualified name.
     * @return name debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(type.toString());
        if (explicit) {
            builder.append('<');
            String collect = members.stream()
                .map(GenericArgument::toString)
                .collect(Collectors.joining(", "));
            builder.append(collect).append('>');
        }
        return builder.toString();
    }
}
