package org.voidlang.compiler.node.type.generic;

import dev.inventex.octa.console.ConsoleFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Get the string representation of the generic argument list.
     * @return name debug information
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!explicit)
            return builder.toString();
        builder.append(ConsoleFormat.CYAN).append('<');
        String collect = generics.stream()
            .map(GenericArgument::toString)
            .collect(Collectors.joining(ConsoleFormat.CYAN + ", "));
        return builder.append(collect)
            .append(ConsoleFormat.CYAN)
            .append('>')
            .toString();
    }

    /**
     * Create a generic type list wrapper that does not have any explicit generic types.
     * @return empty implicit generic type list wrapper
     */
    public static GenericArgumentList implicit() {
        return new GenericArgumentList(new ArrayList<>(), false);
    }

    /**
     * Create a generic type list wrapper that explicitly declares a diamond operator.
     * @return empty explicit generic type list wrapper
     */
    public static GenericArgumentList explicit() {
        return new GenericArgumentList(new ArrayList<>(), true);
    }
}
