package org.voidlang.compiler.node.type.generic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a holder of a generic argument list for a type use.
 * <p>Example:</p>
 * <pre> {@code
 *     TypeWithGeneric<GenericArgument<NestedArgument1, NestedArgument2>, OtherGenericArgument>
 * } </pre>
 */
@AllArgsConstructor
@Getter
public class GenericArgumentList implements Iterable<GenericArgument> {
    /**
     * The generic argument tokens of the type. If the diamond operator is used,
     * this list is an empty list.
     */
    @NotNull
    private final List<GenericArgument> generics;

    /**
     * Indicate, whether the generic arguments have been declared explicitly.
     */
    private final boolean explicit;

    /**
     * Indicate, whether the generic argument declaration uses the diamond operator, therefore
     * is relying on the compiler to find the type from its use.
     * @return true if a diamond operator is explicitly declared
     */
    public boolean isDiamond() {
        return explicit && generics.isEmpty();
    }

    /**
     * Returns an iterator over elements of type {@link GenericArgument}.
     * @return a generic type iterator.
     */
    @NotNull
    @Override
    public Iterator<GenericArgument> iterator() {
        return generics.iterator();
    }
}
